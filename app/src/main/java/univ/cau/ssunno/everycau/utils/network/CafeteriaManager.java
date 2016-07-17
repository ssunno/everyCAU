package univ.cau.ssunno.everycau.utils.network;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import univ.cau.ssunno.everycau.utils.database.DatabaseHelper;

public class CafeteriaManager {

    public CafeteriaManager() {
    }

    // 해당 날짜, 해당 타임의 식단 정보
    public ArrayList<CafeteriaInfo> getMeals(String date){  // TODO :  파라미터 값에 따른 요청 읽을 수 있도록 수정
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    DatabaseHelper db = DatabaseHelper.getInstance(null);
                    ArrayList<CafeteriaInfo> cafeteriaInfos = new ArrayList<>();
                    for (CafeteriaInfo ci : requestMealsByCafeteria("20160718", 2))
                        cafeteriaInfos.add(ci);
                    for (CafeteriaInfo ci : requestMealsByCafeteria("20160718", 8))
                        cafeteriaInfos.add(ci);

                    for (CafeteriaInfo ci : cafeteriaInfos){
                        int cafeteria = db.insertCafeteria(0, ci.getCafeteriaCode(), "20160718", ci.getQuarter(), ci.getServiceTime());
                        for (MenuInfo mi : ci.getMenus()) {
                            int menu = db.insertMenu(cafeteria, mi.getStyle(), mi.getPrice());
                            for (String dish : mi.getDishs())
                                db.insertDish(menu, dish);
                        }
                    }
                } catch ( Exception e) {e.printStackTrace();}
            }
        }).start();
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(null);
        return dbHelper.getMealsFromDB(0, date, 2);
    }

    private ArrayList<CafeteriaInfo> requestMealsByCafeteria(String date, int cafeteriaValue) throws Exception{
        URL url = new URL("http://cautis.cau.ac.kr/SMT/tis/sMwsFoo010/selectList.do");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Accept-Language", "ko-kr,ko;q=0.8,en-us;q=0.5,en;q=0.3");

        // 데이터
        String param = URLEncoder.encode("today", "UTF-8") + "=" + URLEncoder.encode(date, "UTF-8");
        param += "&" + URLEncoder.encode("calvalue", "UTF-8") + "=" + URLEncoder.encode("0", "UTF-8");
        param += "&" + URLEncoder.encode("store", "UTF-8") + "=" + URLEncoder.encode(String.format("%02d", cafeteriaValue), "UTF-8");

        // 전송
        OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
        osw.write(param);
        osw.flush();
        // XML 파서 생성
        XmlPullParser xmlPullParser = XmlPullParserFactory.newInstance().newPullParser();
        xmlPullParser.setInput(conn.getInputStream(), "UTF-8");

        int eventType = xmlPullParser.getEventType();
        ArrayList<CafeteriaInfo> cafeteriaInfos = new ArrayList<>();
        String tagName = null, serviceTime = "", type = null, price = null;
        ArrayList<MenuInfo> menuInfos = new ArrayList<>();
        ArrayList<String> dishList = new ArrayList<>();
        int quarter = 0;
        while (eventType != XmlPullParser.END_DOCUMENT){
            switch (eventType){
                case XmlPullParser.START_TAG:
                    tagName = xmlPullParser.getName();
                    break;
                case XmlPullParser.END_TAG:
                    tagName = "";
                    break;
                case XmlPullParser.TEXT:
                    switch (tagName){
                        case "raw":
                            if( type != null && price != null && !serviceTime.equals("")) {
                                dishList.remove(0);
                                menuInfos.add(new MenuInfo(type, price, dishList));
                                type = null;
                                price = null;
                                cafeteriaInfos.add(new CafeteriaInfo(0, cafeteriaValue, quarter, serviceTime, menuInfos));
                                serviceTime ="";
                                menuInfos = new ArrayList<>();
                                dishList = new ArrayList<>();
                            }
                            // new menu
                            break;
                        case "menunm":
                            String[] menunm = xmlPullParser.getText().split("\\(");
                            serviceTime += menunm[0] + " ";
                            switch(menunm[0]) {
                                case "조식": quarter = 1; break;
                                case "중식":case "특식": quarter = 2; break;
                                case "석식": quarter = 3; break;
                                default: quarter = 0; break;
                            }
                            // TODO : 어떤 식당은 A(B) 에서 A가 타입 인데, 어디는 B가 타입이라서 맞춰야됨

                            try {type = menunm[1].split("\\)")[0];}
                            catch (ArrayIndexOutOfBoundsException e) { type = menunm[0]; }
                            break;
                        case "tm":
                            serviceTime += "(" + xmlPullParser.getText() + ")";
                            break;
                        case "amt": // 가격
                            price = xmlPullParser.getText();
                            break;
                        case "menunm1": // 메뉴 리스트
                            for (String dish : xmlPullParser.getText().split("<br>"))
                                dishList.add(dish);
                            break;
                    }
                    break;
                default:
                    break;
            }
            eventType = xmlPullParser.next();
        }
        return cafeteriaInfos;
    }
}

