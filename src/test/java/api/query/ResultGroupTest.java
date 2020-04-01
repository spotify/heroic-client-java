package api.query;

import static org.junit.Assert.assertNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import com.spotify.heroic.client.api.query.ResultGroup;
import org.junit.jupiter.api.Test;

public class ResultGroupTest {

  private ObjectMapper mapper = new ObjectMapper().registerModule(new KotlinModule());

  @Test
  public void nullKey() throws JsonProcessingException {
    String json = "{\"type\": \"points\",\"hash\": \"2362f9de\", \"shard\": {},"
                  + "\"cadence\": 0,\"values\": [],\"key\": null,\"tags\": {},"
                  + "\"tagCounts\": {},\"resource\": {},\"resourceCounts\": {}}";
    final ResultGroup.Points points = mapper.readValue(json, ResultGroup.Points.class);
    assertNull(points.getKey());
  }

}
