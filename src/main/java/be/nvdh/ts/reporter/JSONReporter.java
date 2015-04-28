package be.nvdh.ts.reporter;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.LocalDateTime;

import be.nvdh.ts.domain.FetchResult;
import be.nvdh.ts.exception.ReportException;
import be.nvdh.ts.report.Reporter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;

public class JSONReporter implements Reporter {
	
	public static final String REPORTER_SHORTNAME = "JSON";
	public static final String JSON_CONTEXT_KEY =  "JSON";
	public static final String CONFIG_LOCATION = "location";
	public static final String CONFIG_SILENT = "silent";
	
	private File jsonReport;
	
	private String location;
	private Boolean silent;
	
	private static final Logger log = LogManager.getLogger(JSONReporter.class);
	
	public void publish(FetchResult fetchResult, Map<String, String> context) throws ReportException {
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.registerModule(new JodaModule());
			mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
			mapper.enable(SerializationFeature.INDENT_OUTPUT);
			mapper.configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS , false);
			String json = mapper.writeValueAsString(fetchResult);
			context.put(JSON_CONTEXT_KEY, json);
			saveJSON(json);
		} catch (JsonProcessingException e) {
			throw new ReportException(e);
		} catch (IOException e) {
			throw new ReportException("Problem writing json file. If not needed, enable silent mode to go on.", e);
		}
	}

	public void init(Map<String, String> config) {
		
		try {
			location = config.get(CONFIG_LOCATION);
			silent = Boolean.parseBoolean(config.get(CONFIG_SILENT));
			
			if (StringUtils.isEmpty(location)){
				location = ".";
			}
			
			if (!silent){
				jsonReport = new File(location + File.separator + "ts-" + new LocalDateTime().toString("YYYMMDDmmss") + ".json");
				jsonReport.createNewFile();
			}

		} catch (IOException e) {
			log.error("Problem writing json file. " + e.getMessage());
			log.error("Resuming with the in memory representation in the context for the next reporters in the chain.");
		}
	}

	public String getName() {
		return REPORTER_SHORTNAME;
	}
	
	private void saveJSON(String json) throws IOException {
		if (!silent){
			FileUtils.writeStringToFile(jsonReport, json, true);
			log.info("File written to " + jsonReport.getAbsolutePath());
		}
	}
	
	public String toString(){
		return REPORTER_SHORTNAME;
	}

}
