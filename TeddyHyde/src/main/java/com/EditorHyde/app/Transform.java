package com.EditorHyde.app;

import android.content.Context;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: xrdawson
 * Date: 4/4/13
 * Time: 8:33 AM
 * To change this template use File | Settings | File Templates.
 */
public class Transform {

    public int version;
    public String type;
    public String prompt;
    public String code;
    public String name;
    public HashMap context;

    public Transform() {
	context = new HashMap();
    }

}
