package com.mxm.yourage.application;


import com.avos.avoscloud.AVAnalytics;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVObject;
import com.mxm.yourage.bean.Person;

import android.app.Application;

public class AvosApplication extends Application {

	 @Override
	  public void onCreate() {
	    super.onCreate();
	    // ��ʼ��Ӧ�� Id �� Ӧ�� Key����������Ӧ�����ò˵����ҵ���Щ��Ϣ
	    AVOSCloud.initialize(this, "pwmurrmm1nag79qfa1os14a6vdagrl6w1481fnjv23fzbi9h",
	        "lbvmvmr0xy6f6fenvj3q177g7hdm8zoeqjbu0dmq265mbuzi");
	    // ���ñ������󱨸�
	    AVAnalytics.enableCrashReport(getApplicationContext(), true);
	    // ע������
	    AVObject.registerSubclass(Person.class);
	  }


}
