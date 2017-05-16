(defproject securities "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :jvm-opts ["-Xms1024m" "-Xmx2048m"]
  :dependencies [;[org.clojure/clojure "1.5.1"]
                 ;[http.async.client "0.5.2"]
                 [clj-http "1.0.1"]
		 [com.draines/postal "1.11.3"]
		 [me.raynes/fs "1.4.6"]
                 [enlive "1.1.1"]
                 [org.clojure/clojure "1.6.0"]
                 [http.async.client "0.5.2"]
                 [org.slf4j/slf4j-simple "1.7.2"]
                 [clojure-csv/clojure-csv "2.0.1"]
                 [incanter "1.5.6"]
                 [enlive "1.1.1"]

                 [clj-http "1.0.1"]
                 [com.draines/postal "1.11.3"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [http-kit "2.1.18"]
                 [org.slf4j/slf4j-simple "1.7.2"]
                             [clj-http "1.0.1"]
                             [com.draines/postal "1.11.3"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [http-kit "2.1.18"]
                 ;[amalloy/ring-buffer "1.2"]
                 ;[circular-buffer "0.1.0-SNAPSHOT"]
                 [prismofeverything/ring-buffer "1.0.0"]
                 ;;ib gateway requires
                 [com.ib/jtsclient "9.68"]
                 [org.clojure/tools.logging "0.2.3"]
                 [clj-logging-config "1.9.10"]
                 [clojure-watch "LATEST"]
                 [net.mikera/vectorz-clj "0.43.0"]
[me.raynes/conch "0.8.0"]

]
  ;:source-paths ["."]
 :plugins [[lein2-eclipse "2.0.0"]
           [lein-codox "0.9.4"]
             ]
;  :main securities.fiveminute
  ;:main securities.core
  :main securities.index
  ;:main securities.news

;:aot :all

  )
