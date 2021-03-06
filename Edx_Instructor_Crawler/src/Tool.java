package src;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;



public class Tool {
	
	boolean endData=false;
	int page = 1;
	int index = 0;
	ArrayList<Integer> pagelist = new ArrayList<Integer>();
	ArrayList<String> instructorList = new ArrayList<String>();
	
	final WebDriver driver = new FirefoxDriver();
	Data courseData = new Data();

	int urlSize = 0;
	
	public ArrayList<String> getVideoURL() throws IOException{
		
		final ArrayList<String> urlList = new ArrayList<String>();
		
		FileInputStream inputFile = new FileInputStream("./instructor_url.txt");
		
		// Construct BufferReader from inputStreamReader
		BufferedReader fileReader = new BufferedReader(new InputStreamReader(inputFile));
		
		String line = null;
		
		// read url line by line 
		while ( (line = fileReader.readLine()) != null){
			urlList.add(line);
		}
		
		fileReader.close();

		
		
		urlSize = urlList.size();
		System.out.println("size of url - " + urlSize);
		
		return urlList; 
		
	}
		
	/*
	 * End of get All url function
	 */
	
	/*
	 * creatDomRoot
	 * 
	 */
	
	
	org.w3c.dom.Document newCreatedDocument = null;
	
	synchronized public org.w3c.dom.Document createDomRoot(){
		System.out.println("----------------Root create-----------------");

		try {
			newCreatedDocument = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		org.w3c.dom.Node root = newCreatedDocument.createElement("ROOT");
		newCreatedDocument.appendChild(root);
		return newCreatedDocument;
		
	}
	
	// To prevent occurring null pointer exception during transforming XML
	public static Text createTextNodeWithoutNull(Document doc, String str){
		Text textNode;
		if(str != null) textNode = doc.createTextNode(str);
		else textNode = doc.createTextNode("null");
		
		return textNode;
	}
	
	/*
	 * Crawl data.
	 * 
	 */
	
	public void crawlData(final String url, final Integer index) throws IOException{
		
		// open website
		driver.navigate().to(url);
						
		// sleep 10 seconds to wait until onload
		try{
			Thread.sleep(1000*10);
		}catch(Exception e){
			e.printStackTrace();
		}
				
		// newCreatedDocument is destination of XML.
		NodeList nodelist=newCreatedDocument.getElementsByTagName("ROOT");
		Node root=nodelist.item(0);
						
				
		Data data = new Data();
		
				
		data.url = url;
		
		if( driver.findElements(By.cssSelector("h1.instructor-title")).size() < 1){
			return;
			
		}
		
		
		
		// instructor name
		
		data.name = driver.findElement(By.cssSelector("h1.instructor-title")).getText();
		System.out.println(data.name);
		
		// instructor job and institution
		
		
		if(driver.findElements(By.cssSelector("ul.org-roles")).size() > 0){
			ArrayList <WebElement> detailsInfo = (ArrayList<WebElement>) driver.findElement(By.cssSelector("ul.org-roles")).findElements(By.tagName("li"));
			if(detailsInfo.size()>1){
				String temp = new String();
				for(int i=0; i < detailsInfo.size() -1; i++){
					temp = temp + detailsInfo.get(i).getText();
				}
				data.job = temp;
				
				data.institution = detailsInfo.get(detailsInfo.size() -1).getText();
				
				
			}
			
		}else{
			data.job = "null";
			data.institution = "null";
		}
		System.out.println(data.job + " - " + data.institution);
		
		
		// instructor's resume
		data.resume = driver.findElements(By.className("resume-copy")).get(0).getText();
		System.out.println(data.resume);
		
		
		// Teaching courses list
		ArrayList <WebElement> courseList = new ArrayList <WebElement> (driver
				.findElements(By.cssSelector("div.discovery-card")));
		
		data.size_of_courses = Integer.toString(courseList.size());
		System.out.println("courseList Size -  "  + data.size_of_courses);
			
		
		// making dom elements
		
		org.w3c.dom.Element instructor_info = newCreatedDocument.createElement("InstructorInfo");
						
		root.appendChild(instructor_info);
		{
			
			// name
			org.w3c.dom.Element name = newCreatedDocument
					.createElement("name");
			name.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.name));
			instructor_info.appendChild(name);
						
			// job
			org.w3c.dom.Element job = newCreatedDocument.createElement("job");
			job.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.job));
			instructor_info.appendChild(job);
			
			// institution
			org.w3c.dom.Element institution = newCreatedDocument.createElement("institution");
			institution.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.institution));
			instructor_info.appendChild(institution);

			// number of courses
			org.w3c.dom.Element numberCourses = newCreatedDocument.createElement("numberOfCourses");
			numberCourses.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.size_of_courses));
			instructor_info.appendChild(numberCourses);

			// courses
			org.w3c.dom.Element courses = newCreatedDocument.createElement("Courses");
			for(WebElement element : courseList){
				org.w3c.dom.Element course = newCreatedDocument
						.createElement("course");
				
				// 현재 courseCode + title 형태의 string으로 저장되어있음.
				course.appendChild(createTextNodeWithoutNull
						(newCreatedDocument, element.findElement(By.className("course-code")).getText() ));
				course.appendChild(createTextNodeWithoutNull
						(newCreatedDocument, element.findElement(By.className("title")).getText() ));
				
				System.out.println(element.findElement(By.className("title")).getText() );

				courses.appendChild(course);
				
			}
			instructor_info.appendChild(courses);
			
			// resume
			org.w3c.dom.Element resume = newCreatedDocument.createElement("resume");
			resume.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.resume));
			instructor_info.appendChild(resume);
			
			// url
			org.w3c.dom.Element course_url = newCreatedDocument.createElement("url");
			course_url.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.url));
			instructor_info.appendChild(course_url);
			
								
		}

		if(index == urlSize -1){
			System.out.println("Last page");
			driver.close();
			
			
		}
	
	}
	
	

	
}
