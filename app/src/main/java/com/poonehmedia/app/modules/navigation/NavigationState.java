package com.poonehmedia.app.modules.navigation;

public class NavigationState {

    /**
     * message to show when error occurs while navigation
     */
    public static String errorMessage = "";

    /**
     * the {@link NavigationArgs} containing all info of the next destination.
     */
    public static NavigationArgs args;


    public static class Loading extends NavigationState {
    }

    public static class Error extends NavigationState {
        public Error(String message) {
            NavigationState.errorMessage = message;
        }
    }

    public static class Success extends NavigationState {
        public Success(NavigationArgs navigationArgs) {
            NavigationState.args = navigationArgs;
        }
    }
}