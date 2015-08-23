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
  [["-c" "--config-file CONFIG-FILE" "Config file "]
   ["-h" "--help"]])

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn  load-resource
  [config-file]
  (let [thr (Thread/currentThread)
        ldr (.getContextClassLoader thr)]
    (read-string (slurp (.getResourceAsStream ldr config-file)))))

(defn -main
  "main func "
  [& args]
  (let [{:keys [options arguments summary errors]} (parse-opts args cli-options)]
    (cond
      (:help options) (exit 0 summary)
      (not (:config-file options)) (exit 1 (str "config-file not passed usage=" summary))
      errors (exit 2 error-msg errors))
    (let [{:keys [config-file]} options
          cprops (load-resource config-file)]
      (log/info "running with config-file=" config-file " edn.data=" cprops)
      (try
        (torna/read-kafka cprops handle-kafka-batch)
        (catch Exception e
          (do
            (.printStackTrace e)
            (System/exit 2)))))))
