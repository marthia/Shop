package com.poonehmedia.app.modules.navigation;

public class NavigationUrl {
    /**
     * the full path url.
     */
    private String link;

    /**
     * denotes whether the link must be opened in external browser or in the app.
     */
    private boolean isExternal = false;
    /**
     * denotes whether the url is a valid phone number.
     */
    private boolean isPhone = false;

    /**
     * denotes whether the url is a valid email address
     */
    private boolean isMail = false;


    public boolean isPhone() {
        return isPhone;
    }

    public void setPhone(boolean phone) {
        isPhone = phone;
    }

    public boolean isMail() {
        return isMail;
    }

    public void setMail(boolean mail) {
        isMail = mail;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public boolean isExternal() {
        return isExternal;
    }

    public void setExternal(boolean external) {
        isExternal = external;
    }
}
