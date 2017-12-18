package com.wrld.widgets.searchbox;

public interface SuggestionProvider extends SearchProvider {

    String getSuggestionTitleFormatting();

    void getSuggestions(String text);

    void addOnSuggestionsReceivedCallback(OnResultsReceivedCallback callback);
    void removeOnSuggestionsReceivedCallback(OnResultsReceivedCallback callback);

    void setSuggestionViewFactory(SearchResultViewFactory factory);
    SearchResultViewFactory getSuggestionViewFactory();
}
