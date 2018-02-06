package com.wrld.widgets.searchbox.model;

import java.util.List;

public interface IOnSearchListener
{
    void onSearchQueryStarted(final SearchQuery query);
    void onSearchQueryCompleted(final SearchQuery query, final List<SearchProviderQueryResult> results);
    void onSearchQueryCancelled(final SearchQuery query);
}
