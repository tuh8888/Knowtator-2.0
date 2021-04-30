(ns knowtator.analogy.subs
  (:require [re-frame.core :refer [reg-sub]]
            [knowtator.model :as model]
            [knowtator.util :as util]
            [com.rpl.specter :as sp]
            [mops.core :as mops]
            [mops.records :as mr]
            [sme-clj.typedef :as types]))

(def default-mop-map
  (as-> (mr/make-mop-map) m
    (types/initialize-kg m)
    (reduce
     (partial apply types/add-entity)
     m
     [[:mass ::types/Function nil ::types/Entity]
      [:charge ::types/Function nil ::types/Entity]
      [:attracts ::types/Function nil ::types/Entity ::types/Entity]
      [:revolve-around ::types/Function nil ::types/Entity ::types/Entity]
      [:temperature ::types/Function nil ::types/Entity]
      [:gravity ::types/Function nil ::types/Expression ::types/Expression]
      [:opposite-sign ::types/Function nil ::types/Expression
       ::types/Expression]
      [:greater ::types/Relation nil ::types/Entity ::types/Entity]
      [:cause ::types/Relation nil ::types/Expression ::types/Expression]
      [:and ::types/Relation {:ordered? false} ::types/Expression
       ::types/Expression] [:Sun ::types/Entity nil]
      [:Planet ::types/Entity nil] [:Nucleus ::types/Entity nil]
      [:Electron ::types/Entity nil]])
    (apply types/add-concept-graph
           m
           :solar-system
           (let [attracts    [:attracts :Sun :Planet]
                 mass-sun    [:mass :Sun]
                 mass-planet [:mass :Planet]]
             [[:cause [:and [:greater mass-sun mass-planet] attracts]
               [:revolve-around :Planet :Sun]]
              [:greater [:temperature :Sun] [:temperature :Planet]]
              [:cause [:gravity mass-sun mass-planet] attracts]]))
    (apply types/add-concept-graph
           m
           :rutherford-atom
           [[:greater [:mass :Nucleus] [:mass :Electron]]
            [:revolve-around :Electron :Nucleus]
            [:cause [:opposite-sign [:charge :Nucleus] [:charge :Electron]]
             [:attracts :Nucleus :Electron]]])
    (mops/infer-hierarchy m)))

(reg-sub ::analogy-graphs
  (fn [_ _]
    (->> [(-> default-mop-map
              (assoc :id :default))]
         (sort-by (comp name :id) util/compare-alpha-num)
         (#(or % [])))))

(reg-sub ::selected-mop-map
  :<- [::analogy-graphs]
  :<- [::selected-analogy-graph-id]
  (fn [[graphs id] _]
    (->> graphs
         (sp/select-one [(sp/filterer #(= id (:id %))) sp/FIRST]))))

(reg-sub ::concept-graphs-for-selected-mop-map
  :<- [::selected-mop-map]
  (fn [mm _]
    (->> mm
         :mops
         keys
         (filter #(mops/abstr? mm % :sme-clj.typedef/ConceptGraph))
         (map #(mops/get-mop mm %))
         (map #(assoc %
                      :id
                      (-> %
                          meta
                          :id)))
         vec)))

(reg-sub ::selection :selection)

(reg-sub ::selected-analogy-graph-id
  :<- [::selection]
  (fn [selected _] (:ana-graphs selected)))

(reg-sub ::selected-node-label (fn [_ _] :id))
(reg-sub ::selected-edge-label (fn [_ _] :predicate))

(reg-sub ::selected-graph
  :<- [::selected-mop-map]
  :<- [::selected-concept-graph-id]
  (fn [[mm concept-graph-id] _]
    (-> mm
        (model/mop-map->graph {:concept-graph #{concept-graph-id}}))))

(reg-sub ::selected-concept-graph-id
  :<- [::selection]
  :<- [::concept-graphs-for-selected-mop-map]
  (fn [[selected choices] _]
    (get selected :concept-graphs (:id (first choices)))))

(reg-sub ::selected-analogy-graph
  :<- [::selected-graph]
  :<- [::selected-node-label]
  :<- [::selected-edge-label]
  (fn [[graph node-label edge-label] _]
    (-> graph
        (update :nodes (partial map #(assoc % :label (get % node-label))))
        (update :edges (partial map #(assoc % :label (get % edge-label))))
        (#(or %
              {:nodes []
               :edges []})))))

(defn all-roles
  [mm]
  (->> mm
       :mops
       vals
       (reduce
        (fn [m mop]
          (->> mop
               (reduce (fn [m [role fillers]]
                         (update m
                                 role
                                 (fn [role-m]
                                   (-> role-m
                                       (update :fillers (fnil into #{}) fillers)
                                       (update :count + (count fillers))
                                       (assoc :mop (mops/get-mop mm role))
                                       (assoc :id role)))))
                       m)))
        {})))

(reg-sub ::selected-mop-map-roles-map
  :<- [::selected-mop-map]
  (fn [mm _]
    (-> mm
        all-roles)))

(reg-sub ::selected-mop-map-roles
  :<- [::selected-mop-map-roles-map]
  (fn [m _]
    (-> m
        vals)))

(reg-sub ::fillers-for-role
  :<- [::selected-mop-map-roles-map]
  (fn [roles-map [_ role]]
    (->> (get-in roles-map
                 [role :fillers])
         (map (partial hash-map :id)))))
