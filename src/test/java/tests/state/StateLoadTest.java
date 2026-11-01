package tests.state;

import java.util.List;
import java.util.Map;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import base.BaseTest;
import core.ConfigReader;
import flows.StateLoadFlow;
import models.StateData;
import pages.common.LoginPage;
import utils.ExcelDataReader;

public class StateLoadTest extends BaseTest{
	
	@Test(dataProvider = "files")
	public void stateLoadTest(String file) throws InterruptedException {
		
        Map<String, List<StateData>> data = ExcelDataReader.getCountryStateData(file, "Sheet1");

		
        new LoginPage().login(ConfigReader.getProperty("username"), ConfigReader.getProperty("password"));
        new StateLoadFlow().run(data);
	
	}
	
	@DataProvider(name = "files")
	public Object[][] excelData(){
        String dir = System.getProperty("user.dir") + "/src/test/resources/testdata/";
        return new Object[][] {
            { dir + "states_sample.xlsx" }
            // add more files here later
        };
	}

}
