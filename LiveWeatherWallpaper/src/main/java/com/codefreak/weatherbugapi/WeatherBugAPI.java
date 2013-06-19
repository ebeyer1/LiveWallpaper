package com.codefreak.weatherbugapi;

import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import java.util.Properties;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * Created by Eric on 6/13/13.
 */
public class WeatherBugAPI {
    public static final String API_KEY = "A5435696365";
    private XPath xpath = XPathFactory.newInstance().newXPath();

    public WeatherBugAPI() {
        xpath.setNamespaceContext(new AWSNameSpaceContext());
    }

    public LiveWeather getLiveWeatherByZipCode(String zipCode) {
        try {
            String url = "http://" + API_KEY + ".api.wxbug.net/getLiveWeatherRSS.aspx?ACode=" + API_KEY + "&zipCode=" + zipCode + "&unitType=0&outputType=1";

            InputSource inputSource = new InputSource(url);

            Node node = (Node)xpath.evaluate("//aws:weather", inputSource, XPathConstants.NODE);

            LiveWeather weather = new LiveWeather(xpath, node);

            return weather;
        } catch (XPathExpressionException e) {

        }
        return null;
    }

    public LiveWeather getLiveWeatherByLatAndLong(double lat, double lng)
    {
        try {
            String url = String.format("http://api.wxbug.net/getLiveWeatherRSS.aspx?ACode=%s&lat=%.2f&long=%.2f&unittype=1&outputtype=1", API_KEY, lat, lng);

            InputSource inputSource = new InputSource(url);

            Node node = (Node)xpath.evaluate("//aws:weather", inputSource, XPathConstants.NODE);

            LiveWeather weather = new LiveWeather(xpath, node);

            return weather;
        } catch (XPathExpressionException e) {

        }
        return null;
    }
}
