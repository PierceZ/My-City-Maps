package com.piercezaifman.mycitymaps;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds references and value event listeners so they can be removed from said listeners. If they don't get removed
 * it can cause memory leaks.
 * <p>
 * Created by piercezaifman on 2017-01-11.
 */

public class FirebaseReferenceHolder {

    private Map<DatabaseReference, List<ValueEventListener>> mReferenceMap = new HashMap<>();

    /**
     * Add the listener to the database reference.
     */
    public void addValueEventListener(DatabaseReference ref, ValueEventListener listener) {
        List<ValueEventListener> listenerList;
        if (mReferenceMap.containsKey(ref)) {
            listenerList = mReferenceMap.get(ref);
        } else {
            listenerList = new ArrayList<>();
            mReferenceMap.put(ref, listenerList);
        }
        listenerList.add(listener);
        ref.addValueEventListener(listener);
    }

    /**
     * Removes all listeners from their database references.
     */
    public void cleanup() {
        for (Map.Entry<DatabaseReference, List<ValueEventListener>> entry : mReferenceMap.entrySet()) {
            DatabaseReference ref = entry.getKey();
            List<ValueEventListener> listenerList = entry.getValue();
            for (ValueEventListener listener : listenerList) {
                ref.removeEventListener(listener);
            }
        }
        mReferenceMap.clear();
    }
}
