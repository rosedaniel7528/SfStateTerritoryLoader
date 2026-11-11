package tests.state;

import java.util.List;
import java.util.Map;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import base.BaseTest;
import core.ConfigReader;
import core.SessionLoader;
import flows.StateLoadFlow;
import models.StateData;
import pages.common.LoginPage;
import utils.ExcelDataReader;

public class StateLoadTest extends BaseTest{
	
	@Test(dataProvider = "countries")
	public void stateLoadTest(String country, List<StateData> states) throws InterruptedException {
		
        if (!SessionLoader.hasSavedSession()) {
            new LoginPage().login(ConfigReader.getProperty("username"), ConfigReader.getProperty("password"));
        }
        new StateLoadFlow().run(Map.of(country,states));
	}
	
	@DataProvider(name = "countries", parallel = true)
	public Object[][] countries(){
	    String file = System.getProperty("user.dir") + "/src/test/resources/testdata/states_sample.xlsx";
	    Map<String, List<StateData>> data = ExcelDataReader.getCountryStateData(file, "Sheet1");
	    return data.entrySet().stream()
	            .map(e -> new Object[] { e.getKey(), e.getValue() })
	            .toArray(Object[][]::new);
	}

}
