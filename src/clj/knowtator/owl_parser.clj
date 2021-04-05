(ns knowtator.owl-parser
  (:require [tawny.owl :as to]
            [tawny.query :as tq]
            [tawny.protocol :as tp]))

(defn load-ontology [f]
  (.loadOntologyFromOntologyDocument (to/owl-ontology-manager) (to/iri f)))

(defn collapse-data [[type & vs]]
  (cond
    (#{:annotation :label :comment} type)      (->> [:type type]
                                                 (conj vs)
                                                 (reduce
                                                   (fn [m [id & vs]]
                                                     (let [vs (cond (= :literal id)    (apply hash-map (conj vs :value))
                                                                    (empty? (rest vs)) (first vs)
                                                                    :else              vs)]
                                                       (assoc m id vs)))
                                                   {}))
    (#{:iri} type)                             (first vs)
    (#{:some :and :not :or :only :oneof} type) {:type type
                                                :data (->> vs
                                                        (map collapse-data)
                                                        set)}
    (#{:has-value} type)                       {:type type
                                                :data (->> vs
                                                        (map collapse-data)
                                                        vec)}
    (#{:at-least} type)                        {:type  type
                                                :value (first vs)
                                                :data  (->> vs
                                                         rest
                                                         (map collapse-data)
                                                         set)}
    :else                                      [type vs]))

(defn ->iri [e]
  (-> e
    tp/as-iri
    bean
    (select-keys [:fragment :namespace])))

(defn into-map [c ks singles]
  (as-> c m
    (tq/into-map m)
    (assoc m :iri (->iri c))
    (reduce (fn [m k]
              (cond-> m (contains? m k) (update k (partial mapv collapse-data))))
      m ks)
    (reduce
      (fn [m k]
        (cond-> m (contains? m k) (update k first)))
      m singles)))

(defn parse-ontology [ontology]
  (let [owl-groups {:obj-props   [tq/obj-props [:inverse :super :annotation :domain :range] [:characteristic :type :inverse]]
                    :classes     [tq/classes [:annotation :disjoint :super :equivalent] [:type]]
                    :ann-props   [tq/ann-props [] [:type]]
                    :data-props  [tq/data-props [] []]
                    :individuals [tq/individuals [] []]}]
    (->> owl-groups
      (map (fn [[k [f ks singles]]] [k (->> ontology
                                        f
                                        (map #(into-map % ks singles)))]))
      (into {:ontology (into-map ontology [:annotation] [:type])}))))