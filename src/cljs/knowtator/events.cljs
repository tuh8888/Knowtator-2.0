(ns knowtator.events
  (:require
   [re-frame.core :as re-frame]
   [knowtator.db :as db]
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [knowtator.model :as model]))

(re-frame/reg-event-db
 ::initialize-db
 (fn-traced [_ _]
   db/default-db))

(re-frame/reg-event-db
 ::set-active-panel
 (fn-traced [db [_ active-panel]]
   (assoc db :active-panel active-panel)))

(re-frame/reg-event-db
  ::set-re-pressed-example
  (fn [db [_ value]]
    (assoc db :re-pressed-example value)))

(re-frame/reg-event-fx
  ::record-selection
  (fn [{:keys [db]} [_ {:keys [start end] :as text-range} doc-id]]
    (cond-> {:db (update db :selection merge text-range)}
      (= start end) (assoc :dispatch [::select-span start doc-id]))))

(re-frame/reg-event-db
  ::select-span
  (fn [db [_ loc doc-id]]
    (let [anns (:anns db)
          span-id         (->> db
                            :spans
                            (model/filter-in-doc doc-id anns)
                            (model/spans-containing-loc loc)
                            vals
                            first
                            :id)]
      (assoc-in db [:selection :span] span-id))))