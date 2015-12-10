package org.dhis2.messenger.core.rest;

import android.util.Log;

import org.dhis2.messenger.model.CopyAttributes;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vladislav on 12/4/15.
 * This is a cache list.
 * It holds cached elements in a list and works with pages.
 * The type must implement the CopyAttributes interface.
 */
public class CacheList<T extends CopyAttributes<T>> {

    //InboxFragment list of pages(lists of InboxModels)
    //private List<ArrayList<InboxModel>> cacheList = new ArrayList<ArrayList<InboxModel>>();
    private int currentPage = 1;
    private int pageSize = 10;
    private int totalPages = 0;
    private int unread = 0; //count of new entries.

    private ArrayList<T> cacheList = new ArrayList<>();

    //-------------------------Page set/get...etc --------------------
    public void setPageSize(int size) {
        this.pageSize = size;
    }

    public int getPageSize() {
        return this.pageSize;
    }

    public void setCurrentPage(int p) {
        this.currentPage = p;
    }

    public int getCurrentPage() {
        return this.currentPage;
    }

    public void setTotalPages(int pages) {
        this.totalPages = pages;
    }
    public int getTotalPages() {
        return this.totalPages;
    }

    //---------------------Get/Remove element by index ----------------
    /**
     * Returns the element at that index.
     * @param index 0 < index < size or you get an exception !
     * @return the element
     * Throws indexOutOfBounds if index is out of bounds
     */
    public T getElement(int index) {
        return cacheList.get(index);
    }

    /**
     * Removes the element from the list.
     * @param index
     */
    public void removeElement(int index) {
        if(index >= 0 && index < cacheList.size()) {
            cacheList.remove(index);
        }
    }

    //-------------------------------Set/Get page--------------------------------------
    /**
     * Add a page of elements to the cache.
     * Merges the changes to the existing cache list.
     * @param page
     * @param newPageList
     * @return Number of new entries added.
     */
    public synchronized int setListPage(int page, List<T> newPageList) {
        int index = (page - 1) * pageSize;
        int nrNew = 0;

        if (cacheList.isEmpty()) {
            cacheList.addAll(index, newPageList);
            nrNew += newPageList.size();

        } else if (page == 1) { // Insert at the front:

            // Find the index in the new list where the cached conversations end.
            // Only this part is new and needs to be added to the cache
            int newOverlapIx = newPageList.indexOf(cacheList.get(0));

            if (newOverlapIx > -1) {

                // Modify the old entries:
                // From overlapIx to end of page update read flag, mod date...+other fields.
                int oldIx = 0;
                for (int newIx = newOverlapIx; newIx < newPageList.size(); newIx++) {
                    if (cacheList.get(oldIx).copyAttributesFrom(newPageList.get(newIx))) {
                        nrNew++;
                    }
                    oldIx++;
                }
                // add the new entries:
                List<T> newElements = newPageList.subList(0, newOverlapIx);
                cacheList.addAll(index, newElements);
                nrNew += newElements.size();
            } else { //all new:
                cacheList.addAll(index, newPageList);
                nrNew += newPageList.size();
            }
        } else {
            if (page > totalPages) {
                totalPages = page;
            }
            int last = page * pageSize;
            if (last > cacheList.size()) {
                last = cacheList.size();
            }
            int newOverlapIx = newPageList.indexOf(cacheList.get(last - 1));

            if (newOverlapIx > -1) { // there is overlap:

                // Update the old ones:
                // From oldIx to end of cacheList :update read flag, mod date...+other fields.
                int oldIx = (cacheList.size() -1) - newOverlapIx;
                for(int newIx = 0; newIx <= newOverlapIx; newIx++) {
                    if (cacheList.get(oldIx).copyAttributesFrom(newPageList.get(newIx))) {
                        nrNew++;
                    }
                    oldIx++;
                }
                //add the new ones:
                List<T> newElements = newPageList.subList(newOverlapIx + 1, newPageList.size());
                cacheList.addAll(newElements);
                nrNew += newElements.size();
            } else {//all new:
                cacheList.addAll(newPageList);
                nrNew += newPageList.size();
            }
        }
        //Log.v("CacheList", "nrNew=" + nrNew);
        return nrNew;
        //Log.v(TAG, "(INSERT) page = " + page + " index=" + index + " cacheList.size() = " + cacheList.size());
    }

    /**
     * Returns a list of elements on the page.
     * If the page is not in cache (empty) the returned list will be empty.
     * If the page is not of page size only the available elements are returned.
     *
     * @param page
     * @return list of elements
     */
    public synchronized List<T> getListPage(int page) {
        int index = (page - 1) * pageSize;

        //TODO: move this in the RESTSessionStorage wrapper logic
        //Need to refresh the cache.
        /*if(page == 1 && startedNewConversation()) {
            startedNewConversation(false);
            return cacheList.subList(0, 0); // ie empty list
        }*/

        // If cache is empty return empty list.
        if (index < 0 || totalPages == 0 || page > totalPages) {
            return cacheList.subList(0, 0); // ie empty list
        }

        // For partial end pages:
        if (index + pageSize > cacheList.size()) {
            //Log.v(TAG, "page = " + page + " index=" + index + " index + pageSize = " + (index + pageSize) + " cacheList.size() = " + cacheList.size());
            if (index > cacheList.size()) {
                return cacheList.subList(0,0);
            }
            List<T> toReturn = cacheList.subList(index, cacheList.size());
            //return cacheList.subList(index, cacheList.size());
            return toReturn;
        } else {// Full page:
            return cacheList.subList(index, index + pageSize);
        }
    }


}
