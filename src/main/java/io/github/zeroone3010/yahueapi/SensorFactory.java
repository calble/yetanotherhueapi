package io.github.zeroone3010.yahueapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.zeroone3010.yahueapi.domain.SensorDto;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.function.Supplier;

final class SensorFactory {
  private final Hue hue;

  SensorFactory(final Hue hue) {
    this.hue = hue;
  }

  Sensor buildSensor(final String id, final SensorDto sensor, final String bridgeUri,
                            final ObjectMapper objectMapper) {
    if (sensor == null) {
      throw new HueApiException("Sensor " + id + " cannot be found.");
    }

    final URL url = buildSensorUrl(bridgeUri, id);

    final SensorType type = SensorType.parseTypeString(sensor.getType());
    final Supplier<Map<String, Object>> stateProvider = createStateProvider(objectMapper, url);
    switch (type) {
      case MOTION:
        return new MotionSensorImpl(id, sensor, url, stateProvider);
      case TEMPERATURE:
        return new TemperatureSensorImpl(id, sensor, url, stateProvider);
      case DAYLIGHT:
        return new DaylightSensorImpl(id, sensor, url, stateProvider);
      case DIMMER_SWITCH:
        return new DimmerSwitchImpl(id, sensor, url, stateProvider);
      default:
        return new BasicSensor(id, sensor, url, stateProvider);
    }
  }

  private Supplier<Map<String, Object>> createStateProvider(final ObjectMapper objectMapper, final URL url) {
    return () -> {
      try {
        return objectMapper.readValue(url, SensorDto.class).getState();
      } catch (IOException e) {
        throw new HueApiException(e);
      }
    };
  }

  private static URL buildSensorUrl(final String bridgeUri, final String sensorId) {
    try {
      return new URL(bridgeUri + "sensors/" + sensorId);
    } catch (final MalformedURLException e) {
      throw new HueApiException(e);
    }
  }

}
