/*
 * To change this license header, choose License Headers in Project properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.qa.testautomation.framework.intefaces;

import java.util.Map;

/**
 * @author txl4x
 */
public interface RestDriver {

    void initialize();

    void close();

    void connect();

    String getCookies();

    Object get(String path);

    //special for query with name param name "query" and value
    Object get(String path, String query);

    Object get(String path, String key, String value);

    Object get(String path, Map<String, String> queries);

    Object post(String path, String payload);

    Object put(String path, String payload);

    Object delete(String path);

}
