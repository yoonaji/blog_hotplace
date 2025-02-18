package first;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.net.URL;
import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;

public class SeleniumTest {
    public static void main(String[] args) {
        System.setProperty("webdriver.chrome.driver", "C:\\drivers\\chromedriver.exe");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox"); // 샌드박스 모드 해제
        options.addArguments("--disable-dev-shm-usage"); // 공유 메모리 문제 방지
        options.addArguments("--remote-allow-origins=*"); // CORS 정책 우회
        options.addArguments("--disable-blink-features=AutomationControlled"); // 자동화 탐지 방지
        options.addArguments("--start-maximized"); // 브라우저 창 최대화
        options.addArguments("--disable-popup-blocking"); // 팝업 차단 해제

        WebDriver driver = new ChromeDriver(options);
        String url = "https://search.naver.com/search.naver?ssc=tab.blog.all&query=%ED%95%AB%ED%94%8C&sm=tab_opt&nso=so%3Ar%2Cp%3A1w";       
        driver.get(url); // 네이버 접속
        JavascriptExecutor js = (JavascriptExecutor) driver;
        
        long lastHeight = (long) js.executeScript("return document.body.scrollHeight");
        while (true) {
        	js.executeScript("window.scrollTo(0, document.body.scrollHeight)");
        	try {
        	Thread.sleep(2000);
        	} catch (InterruptedException e){
        		e.printStackTrace();
        	}
        	long newHeight = (long) js.executeScript("return document.body.scrollHeight");
        	if (newHeight==lastHeight) {
        		System.out.println("스크롤 완료");
        		break;
        	}
        	lastHeight=newHeight;
     
        }
        List<String> elements = driver.findElements(By.cssSelector("a.title_link"))
        								//webelement 리스트를 스트림으로 변환
        								.stream()
        								//href 속성만 추출
        								.map(e -> e.getAttribute("href"))
        								//.filter(href -> href.contains("u="))
        								//.map(href -> URLDecoder.decode(href.split("u=")[1], StandardCharsets.UTF_8))
        								//List<String>으로 변환.
        								.collect(Collectors.toList());
        Map<List<String>, Integer> map = new HashMap<>();
        for (String element: elements) {
        	try{
        		URL blog = new URL(element);
        		URLConnection urlCon = blog.openConnection();
        	} catch(IOException e) {
        		e.printStackTrace();
        	}
        	
        	driver.get(element);
        	try {
            	Thread.sleep(2000);
            	} catch (InterruptedException e){
            		e.printStackTrace();
            	}
        	try {
        		//findElements는 WebElement 리스트를 반환한다. 해당되는 element가 없을 경우 empty list를 반환한다.
        		if( driver.findElements(By.id("mainFrame")).isEmpty()) {
        			System.out.println("iframe 미존재. 다음블로그로 슝~");
        		}
        		else {
        			WebElement iframe = driver.findElement(By.id("mainFrame"));
            		driver.switchTo().frame(iframe);
            		System.out.println("ifraim 내부로 이동 완료");
            	
            		if(!driver.findElements(By.cssSelector("strong.se-map-title")).isEmpty()) {
            			WebElement contentElement = driver.findElement(By.cssSelector("strong.se-map-title"));
                    	WebElement address = driver.findElement(By.cssSelector("p.se-map-address"));
                    	if (map.containsKey(Arrays.asList(contentElement.getText(), address.getText()))) {
                    		List<String> key = Arrays.asList(contentElement.getText(), address.getText());
                    		int value= map.get(key)+1;
                    		map.put(key, value);
                    	}
                    	else {
                    		map.put(Arrays.asList(contentElement.getText(), address.getText()), 1);
                    	}
            		}	
        		}
        		
        	} catch(NoSuchElementException e) {
        		System.out.println("❌ 요소를 찾을 수 없음: " + e);
        	}
        	 	
        }
        
        List<Map.Entry<List<String>, Integer>> hotList = new ArrayList<>(map.entrySet());
        hotList.sort((e1,e2)-> e2.getValue().compareTo(e1.getValue()));
        
        if (hotList.isEmpty()) {
            System.out.println("❌ 크롤링된 데이터가 없습니다.");
            return; // 프로그램 종료
        }
        
        for(int i=0;i<10;i++) {
        	System.out.println("장소: "+ hotList.get(i).getKey().get(0) + "위치 : "+ hotList.get(i).getKey().get(1));
        }
      
    }
 
}