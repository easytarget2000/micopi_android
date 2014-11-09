package com.easytarget.micopi;

/**
 * Created by michel on 06/11/14.
 */
public class ContactCrawler {

    private static Cursor getContacts() {
        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        String[] projection = new String[]{
                "name_raw_contact_id",
                "display_name",
                "photo_id"
        };
        String selection = "in_visible_group = '1'";
        if (Config.getInstance(this).shouldIgnoreContactVisibility())
            selection = null;
        String sortOrder = "display_name COLLATE LOCALIZED ASC";

        return getContentResolver().query(uri, projection, selection, null, sortOrder);
    }
}
