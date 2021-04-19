package com.ashwinkanchana.cansis.utils;

public class Constants {

    public static final String PREF_KEY_USERNAME = "a";
    public static final String PREF_KEY_USER_TYPE = "b";
    public static final String PREF_KEY_IS_FIRST_TIME = "c";
    public static final String PREF_KEY_IS_LOGGED_IN = "d";
    public static final String PREF_KEY_DARK_MODE = "e";
    public static final String PREF_KEY_DEFAULT_SEMESTER = "f";
    public static final String PREF_KEY_PROFILE_PIC_URL = "g";
    public static final String PREF_KEY_BRANCH_SHORT ="h";
    public static final String PREF_KEY_STUDENT_NAME = "i";
    public static final String PREF_KEY_USN = "j";

    public static final String PREF_KEY_MARKS_SUBS_CACHE_SEM = "k";
    public static final String PREF_KEY_MARKS_SUBS_CACHE = "l";
    public static final String PREF_KEY_ATTD_CACHE = "m";
    public static final String PREF_KEY_STUDENT_ID = "n";
    public static final String PREF_KEY_ENCODED_NAME = "o";
    public static final String PREF_KEY_GALLERY_CACHE = "p";
    public static final String PREF_KEY_PARENT_PHONE = "q";
    public static final String PREF_KEY_NOTICE_CACHE = "r";
    public static final String PREF_KEY_PROFILE_CACHE = "s";
    public static final String PREF_KEY_CURRENT_SEM = "t";

    public static final String REQUEST_QUEUE_TAG_NOTICE = "notice";
    public static final String REQUEST_QUEUE_TAG_ATTENDANCE ="attendance";
    public static final String REQUEST_QUEUE_TAG_GALLERY = "gallery";
    public static final String REQUEST_QUEUE_TAG_PROFILE = "profile";


    public static final String ANIM_TYPE_LEFT_TO_RIGHT = "left-to-right";
    public static final String ANIM_TYPE_RIGHT_TO_LEFT ="right-to-left";
    public static final String JSON_ARRAY_RESULT = "result";

    public static final String JSON_ARRAY_VALUE = "value";


    public static final int STATE_EMPTY = 1;
    public static final int STATE_NO_CONNECTION = 2;
    public static final int STATE_UNKNOWN_ERROR = 4;
    public static final int STATE_CONTENT = 5;
    public static final int STATE_LOADING = 6;
    public static final int STATE_RETRY = 7;
    public static final int STATE_CHOOSE_SEM = 8;
    public static final int STATE_CACHE = 9;
    public static final int STATE_CACHE_CONTENT = 10;

    public static final String STATE_CONNECTED = "CONNECTED";
    public static final String STATE_DISCONNECTED = "DISCONNECTED";

    public static final int USER_PARENT = 1;
    public static final int USER_STUDENT = 2;
    public static final int USER_NONE = 0;


    public static final int SOURCE_API = 1;
    public static final int SOURCE_CACHE = 2;

    public static final String[] SEMESTERS = {"-","I","II","III","IV","V","VI","VII","VIII"};

    public static final String STR_USER_PARENT = "Parent";
    public static final String STR_USER_STUDENT = "Student";


    public static final String FS_COLLECTION_USER_STUDENT = "user_student";
    public static final String FS_COLLECTION_USER_PARENT = "user_parent";

    public static final String FS_COLLECTION_STUDENT_DATA = "student_data";

    public static final String FS_COLLECTION_FEEDBACK = "feedbacks";

    public static final String FS_KEY_STUDENT_PHONE = "student_phone";
    public static final String FS_KEY_FEEDBACK = "feedback";
    public static final String FS_KEY_RATING = "rating";
    public static final String FS_KEY_TIMESTAMP = "timestamp";
    public static final String FS_KEY_ATTENDANCE_LIST = "attendance_list";

    public static final String FS_ACCOUNT_UID = "UID";
    public static final String FS_KEY_PHONE = "phone";
    public static final String FS_KEY_PARENT_PHONE = "parent_phone";
    public static final String FS_KEY_EMAIL = "email";
    public static final String FS_KEY_STUDENT_NAME = "name";
    public static final String FS_KEY_STUDENT_FIRST_NAME = "first_name";
    public static final String FS_KEY_CREATED_AT = "created_at";
    public static final String FS_KEY_IMAGE = "imgsrc";
    public static final String FS_KEY_BRANCH = "branch";
    public static final String FS_KEY_PARENT_NAME = "parent";
    public static final String FS_KEY_ADDRESS = "address";
    public static final String FS_KEY_LAST_SEEN = "last_seen";
    public static final String FS_KEY_SEMESTER_COUNT = "semester";
    public static final String FS_KEY_ENCODED_NAME = "encoded_name";
    public static final String FS_KEY_STUDENT_ID = "studentID";
    public static final String FS_KEY_USN = "usn";
    public static final String FS_FCM_TOKEN = "fcm_token";
    public static final String FS_KEY_OTP = "otp";
    public static final String FS_KEY_OTP_COUNT = "otp_count";

    public static final String FCM_TOPIC_ANNOUNCEMENTS = "announcements";
    public static final String FCM_TOPIC_STUDENTS = "students";
    public static final String FCM_TOPIC_PARENTS = "parents";

    public static final String NOTIFICATION_CHANNEL_ANNOUNCEMENTS = "Announcements";
    public static final String NOTIFICATION_CHANNEL_GENERAL = "General";
    public static final String NOTIFICATION_CHANNEL_OTHERS = "Others";

    public static final String PACKAGE_NAME = "com.ashwinkanchana.cansis";
    public static final String CRED = "ashwinkanchana";
    public static final String EMAIL_FROM = "Cansis";
    public static final String EMAIL_TITLE = "Cansis OTP";
    public static final String EMAIL_FROM_NAME = "noreply@DOMAIN.com";
    public static final String EMAIL_PLACEHOLDER_NAME = "{{name}}";
    public static final String EMAIL_PLACEHOLDER_OTP = "{{otp}}";
    public static final String EMAIL_TEXT = "Login via OTP";
    public static final String EMAIL_RESPONSE = "message";
    public static final String EMAIL_SUCCESS = "success";
    public static final String RECAPTCHA_SITE_KEY = "RECAPTCHA_API_KEY";

    public static final String RC_ERROR_TYPE = "error_type";

    public static final String RC_IS_APP_DISABLED = "app_disabled";
    public static final String RC_DISABLED_TITLE = "disabled_title";
    public static final String RC_DISABLED_DESCRIPTION = "disabled_description";
    public static final String RC_DISABLED_BUTTON_TEXT = "disabled_button_text";


    public static final String RC_APP_UPDATE_REQUIRED = "app_update_required";
    public static final String RC_UPDATE_TITLE = "update_title";
    public static final String RC_UPDATE_DESC = "update_desc";
    public static final String RC_UPDATE_URL = "update_url";
    public static final String RC_APP_LATEST_VERSION = "latest_version";



    public static final String RC_SIGNATURE_TEXT = "signature_text";
    public static final String RC_SIGNATURE_URL = "signature_url";
    public static final String RC_SIGNATURE_CLICK_ENABLED = "signature_click_enabled";
    public static final String RC_SIGNATURE_CLICK_SPAN_START = "signature_click_span_start";
    public static final String RC_SIGNATURE_CLICK_SPAN_STOP = "signature_click_span_stop";


    public static final int RC_ERROR_TYPE_UPDATE = 1;
    public static final int RC_ERROR_TYPE_DISABLED = 2;

    public static final String NETWORK_BROADCAST_ACTION  = "com.ashwinkanchana.cansis.NETWORK_STATE_CHANGE";
    public static final String NETWORK_BROADCAST_VALUE = "com.ashwinkanchana.cansis.NETWORK_STATE_IS_CONNECTED";
    public static final String NETWORK_BROADCAST_SOURCE = "com.ashwinkanchana.cansis.NETWORK_STATE_SOURCE";


    public static final String HTML_TEMPLATE = "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "\n" +
            "  <meta charset=\"utf-8\">\n" +
            "  <meta http-equiv=\"x-ua-compatible\" content=\"ie=edge\">\n" +
            "  <title>Cansis OTP</title>\n" +
            "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
            "  <style type=\"text/css\">\n" +
            "  @media screen {\n" +
            "    @font-face {\n" +
            "      font-family: 'Source Sans Pro';\n" +
            "      font-style: normal;\n" +
            "      font-weight: 400;\n" +
            "      src: local('Source Sans Pro Regular'), local('SourceSansPro-Regular'), url(https://fonts.gstatic.com/s/sourcesanspro/v10/ODelI1aHBYDBqgeIAH2zlBM0YzuT7MdOe03otPbuUS0.woff) format('woff');\n" +
            "    }\n" +
            "\n" +
            "    @font-face {\n" +
            "      font-family: 'Source Sans Pro';\n" +
            "      font-style: normal;\n" +
            "      font-weight: 700;\n" +
            "      src: local('Source Sans Pro Bold'), local('SourceSansPro-Bold'), url(https://fonts.gstatic.com/s/sourcesanspro/v10/toadOcfmlt9b38dHJxOBGFkQc6VGVFSmCnC_l7QZG60.woff) format('woff');\n" +
            "    }\n" +
            "  }\n" +
            "\n" +
            "  /**\n" +
            "   * Avoid browser level font resizing.\n" +
            "   * 1. Windows Mobile\n" +
            "   * 2. iOS / OSX\n" +
            "   */\n" +
            "  body,\n" +
            "  table,\n" +
            "  td,\n" +
            "  a {\n" +
            "    -ms-text-size-adjust: 100%; /* 1 */\n" +
            "    -webkit-text-size-adjust: 100%; /* 2 */\n" +
            "  }\n" +
            "\n" +
            "  /**\n" +
            "   * Remove extra space added to tables and cells in Outlook.\n" +
            "   */\n" +
            "  table,\n" +
            "  td {\n" +
            "    mso-table-rspace: 0pt;\n" +
            "    mso-table-lspace: 0pt;\n" +
            "  }\n" +
            "\n" +
            "  /**\n" +
            "   * Better fluid images in Internet Explorer.\n" +
            "   */\n" +
            "  img {\n" +
            "    -ms-interpolation-mode: bicubic;\n" +
            "  }\n" +
            "\n" +
            "  /**\n" +
            "   * Remove blue links for iOS devices.\n" +
            "   */\n" +
            "  a[x-apple-data-detectors] {\n" +
            "    font-family: inherit !important;\n" +
            "    font-size: inherit !important;\n" +
            "    font-weight: inherit !important;\n" +
            "    line-height: inherit !important;\n" +
            "    color: inherit !important;\n" +
            "    text-decoration: none !important;\n" +
            "  }\n" +
            "\n" +
            "  /**\n" +
            "   * Fix centering issues in Android 4.4.\n" +
            "   */\n" +
            "  div[style*=\"margin: 16px 0;\"] {\n" +
            "    margin: 0 !important;\n" +
            "  }\n" +
            "\n" +
            "  body {\n" +
            "    width: 100% !important;\n" +
            "    height: 100% !important;\n" +
            "    padding: 0 !important;\n" +
            "    margin: 0 !important;\n" +
            "  }\n" +
            "\n" +
            "  /**\n" +
            "   * Collapse table borders to avoid space between cells.\n" +
            "   */\n" +
            "  table {\n" +
            "    border-collapse: collapse !important;\n" +
            "  }\n" +
            "\n" +
            "  a {\n" +
            "    color: #1a82e2;\n" +
            "  }\n" +
            "\n" +
            "  img {\n" +
            "    height: auto;\n" +
            "    line-height: 100%;\n" +
            "    text-decoration: none;\n" +
            "    border: 0;\n" +
            "    outline: none;\n" +
            "  }\n" +
            "  .firstClass, .secondClass {display: inline;}\n" +
            "  </style>\n" +
            "\n" +
            "</head>\n" +
            "<body style=\"background-color: #e9ecef;\">\n" +
            "\n" +
            "  <!-- start preheader -->\n" +
            "  <div class=\"preheader\" style=\"display: none; max-width: 0; max-height: 0; overflow: hidden; font-size: 1px; line-height: 1px; color: #fff; opacity: 0;\">\n" +
            "    Cansis • Login via OTP • CEC \n" +
            "  </div>\n" +
            "  <!-- end preheader -->\n" +
            "\n" +
            "  <!-- start body -->\n" +
            "  <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">\n" +
            "\n" +
            "   \n" +
            "\n" +
            "    <!-- start hero -->\n" +
            "    <tr>\n" +
            "      <td align=\"center\" bgcolor=\"#e9ecef\">\n" +
            "        <!--[if (gte mso 9)|(IE)]>\n" +
            "        <table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"600\">\n" +
            "        <tr>\n" +
            "        <td align=\"center\" valign=\"top\" width=\"600\">\n" +
            "        <![endif]-->\n" +
            "        <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"max-width: 600px;\">\n" +
            "          <tr>\n" +
            "            <td align=\"left\" bgcolor=\"#ffffff\" style=\"padding: 36px 24px 0; font-family: 'Source Sans Pro', Helvetica, Arial, sans-serif; border-top: 3px solid #d4dadf;\">\n" +
            "              <h1 style=\"margin: 0; font-size: 32px; font-weight: 700; letter-spacing: -1px; line-height: 48px;\"></h1>\n" +
            "            </td>\n" +
            "          </tr>\n" +
            "        </table>\n" +
            "        <!--[if (gte mso 9)|(IE)]>\n" +
            "        </td>\n" +
            "        </tr>\n" +
            "        </table>\n" +
            "        <![endif]-->\n" +
            "      </td>\n" +
            "    </tr>\n" +
            "    <!-- end hero -->\n" +
            "\n" +
            "    <!-- start copy block -->\n" +
            "    <tr>\n" +
            "      <td align=\"center\" bgcolor=\"#e9ecef\">\n" +
            "        <!--[if (gte mso 9)|(IE)]>\n" +
            "        <table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"600\">\n" +
            "        <tr>\n" +
            "        <td align=\"center\" valign=\"top\" width=\"600\">\n" +
            "        <![endif]-->\n" +
            "        <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"max-width: 600px;\">\n" +
            "\n" +
            "          <!-- start copy -->\n" +
            "\t\t  <tr>\n" +
            "\t\t  <td align=\"left\" bgcolor=\"#ffffff\" style=\"padding-left: 24px; padding-top: 24px;font-family: 'Source Sans Pro', Helvetica, Arial, sans-serif; font-size: 22px; line-height: 24px;\">\n" +
            "              <text style=\"margin: 0; font-family: 'Source Sans Pro', Helvetica, Arial, sans-serif; font-size: 24px;\">Hi,</text>\n" +
            "\t\t\t</td>\n" +
            "\t\t\t\n" +
            "           \n" +
            "          </tr>\n" +
            "          <tr>\n" +
            "\t\t  <td align=\"left\" bgcolor=\"#ffffff\" style=\"padding-left: 24px; font-family: 'Source Sans Pro', Helvetica, Arial, sans-serif; font-size: 22px; line-height: 24px;\">\n" +
            "\t\t\t  <p class = \"firstClass\"; style=\"color:red;  margin: 0;font-family: 'Source Sans Pro', Helvetica, Arial, sans-serif; font-size: 22px;\">{{name}}</p>\n" +
            "\t\t\t  <p class = \"secondClass\";>!</p>\n" +
            "\t\t\t</td>\n" +
            "\t\t\t\n" +
            "           \n" +
            "          </tr>\n" +
            "\t\t   <tr>\n" +
            "\t\t  <td align=\"center\" bgcolor=\"#ffffff\" style=\"padding: 24px; font-family: 'Source Sans Pro', Helvetica, Arial, sans-serif; font-size: 20px; line-height: 24px;\">\n" +
            "              <p style=\"margin: 0;\">Login via OTP<br></p>\n" +
            "            </td>\n" +
            "           \n" +
            "          </tr>\n" +
            "          <!-- end copy -->\n" +
            "\n" +
            "          <!-- start button -->\n" +
            "          <tr>\n" +
            "            <td align=\"left\" bgcolor=\"#ffffff\">\n" +
            "              <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">\n" +
            "                <tr>\n" +
            "                  <td align=\"center\" bgcolor=\"#ffffff\" style=\"padding: 12px;\">\n" +
            "                    <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n" +
            "                      <tr>\n" +
            "                        <td align=\"center\" bgcolor=\"#1a82e2\" style=\"border-radius: 6px;\">\n" +
            "                          <a style=\"display: inline-block; padding: 16px 36px; font-family: 'Source Sans Pro', Helvetica, Arial, sans-serif; font-size: 22px; color: #ffffff; text-decoration: none; border-radius: 6px;\">{{otp}}</a>\n" +
            "                        </td>\n" +
            "                      </tr>\n" +
            "                    </table>\n" +
            "                  </td>\n" +
            "                </tr>\n" +
            "              </table>\n" +
            "            </td>\n" +
            "          </tr>\n" +
            "          <!-- end button -->\n" +
            "\n" +
            "          <!-- start copy -->\n" +
            "          <tr>\n" +
            "            <td align=\"left\" bgcolor=\"#ffffff\" style=\"padding: 24px; font-family: 'Source Sans Pro', Helvetica, Arial, sans-serif; font-size: 16px; line-height: 24px;\">\n" +
            "              <p style=\"margin: 0;\">Do not share your OTP</p>\n" +
            "            </td>\n" +
            "          </tr>\n" +
            "          <!-- end copy -->\n" +
            "\n" +
            "          <!-- start copy -->\n" +
            "          <tr>\n" +
            "            <td align=\"left\" bgcolor=\"#ffffff\" style=\"padding: 18px; font-family: 'Source Sans Pro', Helvetica, Arial, sans-serif; font-size: 16px; line-height: 24px; border-bottom: 3px solid #d4dadf\">\n" +
            "\t\t\t  <p style=\"color:gray; margin: 0;font-family: 'Source Sans Pro', Helvetica, Arial, sans-serif; font-size: 14px;\">This email is autogenerated. If you didn't request this, Please ignore</p>\n" +
            "\t\t\t  <p style=\"margin-top: 40px; font-family: 'Source Sans Pro', Helvetica, Arial, sans-serif; font-size: 22px;\">Cansis</p>\n" +
            "\t\t\t   \n" +
            "            </td>\n" +
            "          </tr>\n" +
            "          <!-- end copy -->\n" +
            "\n" +
            "        </table>\n" +
            "        <!--[if (gte mso 9)|(IE)]>\n" +
            "        </td>\n" +
            "        </tr>\n" +
            "        </table>\n" +
            "        <![endif]-->\n" +
            "      </td>\n" +
            "    </tr>\n" +
            "    <!-- end copy block -->\n" +
            "\n" +
            "  </table>\n" +
            "  <!-- end body -->\n" +
            "\n" +
            "</body>\n" +
            "</html>";

}
