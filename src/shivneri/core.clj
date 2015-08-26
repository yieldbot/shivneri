(ns shivneri.core
  (:require [clojure.tools.logging :as log]
            [prismo.core :as p]
            [torna.core :as torna]
            [clojure.tools.cli :refer (parse-opts)]
            [clojure.string :as string])
  (:use [cheshire.core :as json])
  (:gen-class))

(def get-or-create-cluster (memoize p/create-cluster))

(def get-or-open-bucket (memoize p/open-bucket))

(defn handle-kafka-batch
  [props json-docs]
  (let [cb-docs (map (fn [item]
                       (let [ri (item "ri")]
                         (p/make-raw-json-document ri item :expiry 2592000))) ; 24 * 30 * 3600 seconds = 30 days
                     @json-docs)
        cb-cluster (get-or-create-cluster (get props :couchbase.hosts))
        bucket-name (get props :couchbase.bucketname)
        bucket (apply get-or-open-bucket cb-cluster [bucket-name])]
    (p/multi-insert bucket cb-docs :if-exists :supersede)))

(def cli-options
  [["-g" "--group.id GROUP.ID" "Kafka Consumer group id"]
   ["-k" "--kafka.zk.connect KAFKA.ZK.CONNECT" "zookeeper host:port/chroot e.g localhost:2181/kafka"]
   ["-t" "--topic.name TOPIC.NAME" "Name of the kafka topic to consume"]
   ["-p" "--health.port HEALTH.PORT" "Port to listen for health requests"]
   ["-b" "--couchbase.bucket COUCHBASE.BUCKET" "Name of couchbase bucket to insert kafka messages"]
   ["-c" "--couchbase.hosts COUCHBASE.HOSTS" "Comma separated list of couchbase hosts"]
   ["-s" "--batch.size" "BATCH.SIZE" "Kafka batch processing size"
    :default 1000]
   ["-h" "--help"]])

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn verify-options
  [options summary errors]
  (cond
    (:help options) (exit 0 summary)
    (not (:group.id options)) (exit 1 (str "group.id not passed usage=" summary))
    (not (:kafka.zk.connect options)) (exit 1 (str "kafka.zk.connect not passed usage=" summary))
    (not (:topic.name options)) (exit 1 (str "topic.name not passed usage=" summary))
    (not (:couchbase.bucket options)) (exit 1 (str "couchbase.bucket not passed usage=" summary))
    (not (:couchbase.hosts options)) (exit 1 (str "couchbase.hosts not passed usage=" summary))
    errors (exit 2 error-msg errors)))

(defn -main
  "main func "
  [& args]
  (let [{:keys [options arguments summary errors]} (parse-opts args cli-options)]
    (verify-options options summary errors)
    (log/info "running with props=" (assoc options :couchbase.hosts (into [] (.split (get options :couchbase.hosts) ","))))
    (try
      (torna/read-kafka options handle-kafka-batch)
      (catch Exception e
        (do
          (.printStackTrace e)
          (System/exit 2))))))
