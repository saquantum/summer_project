package uk.ac.bristol.controller;

public class Code {
    public static final int PAGINATION_MAX_LIMIT = 1000;


    public static final int SUCCESS = 20000;
    public static final int INSERT_ERR = 20010;
    public static final int INSERT_OK = 20011;
    public static final int DELETE_ERR = 20020;
    public static final int DELETE_OK = 20021;
    public static final int UPDATE_ERR = 20030;
    public static final int UPDATE_OK = 20031;
    public static final int SELECT_ERR = 20040;
    public static final int SELECT_OK = 20041;

    public static final int BAD_REQUEST = 40000;
    public static final int REGISTER_ERR = 40001;
    public static final int UNAUTHORISED = 40100;
    public static final int LOGIN_TOKEN_ERR = 40101;
    public static final int LOGIN_TOKEN_MISSING = 40111;
    public static final int FORBIDDEN = 40300;
    public static final int NOT_FOUND = 40400;
    public static final int SYSTEM_ERR = 50001;
    public static final int BUSINESS_ERR = 60001;
}
