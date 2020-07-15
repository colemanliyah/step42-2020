// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;


import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import java.util.List;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

public final class UserUtils {

    /**
    * Returns a single Entity object that can then be used in a servlet. 
    * The entityPropertyTitle and entityPropertyValue
    * arguments are relative to the entityName argument. 
    * <p>
    * This method always returns immediately, whether or not the 
    * datastore object exists. If the object does not exist, the entity 
    * returned is null.
    * 
    * @param  entityName           the name of an entity class in the datastore (ex.user, game)
    * @param  entityPropertyTitle  the name of a specific property that the entity class has (usually sessionID or userID)
    * @param  entityPropertyValue  the value of the property that is filtered for
    * @return                      a single entity that has the same value for the property in the parameter 
    */
  public Entity getEntityFromDatastore(String entityName, String entityPropertyTitle, String entityPropertyValue, DatastoreService datastore) {

    if(entityPropertyValue == "" || entityName == "" || entityPropertyTitle == "" || datastore == null){
        return null;
    }

    Filter queryFilter = new FilterPredicate(entityPropertyTitle, FilterOperator.EQUAL, entityPropertyValue);
    Query query = new Query(entityName).setFilter(queryFilter);
     PreparedQuery results = datastore.prepare(query);
     List<Entity> resultsList = results.asList(FetchOptions.Builder.withLimit(1));

    if(resultsList.size() == 0){
        return null;
    }

    Entity entity = resultsList.get(0);
    return entity;
  }

   
   /**
    * This function takes in a list of cookies and matches a sessionID cookie
    * with a specific name/value pair to a user entity with the same name/value
    * pair as one of its properties. This user entity is then returned. 
    * 
    * <p>
    * 
    * @param  cookies    an array of cookies (usually all cookies on front end)
    * @param  datastore  the database where entities are stored
    * @return            a single entity that has the cookie name/value pair as a property
    */
 
  public static Entity getUserFromCookie(Cookie cookies[], DatastoreService datastore){
      if(datastore == null){
          return null;
      }
      String sessionIDName = "SessionID";
      Cookie cookie = cookieUtils.getCookieGivenName(cookies, sessionIDName); //returns null if it doesn't exist
      if(cookie == null){ 
          return null;
      }
      return getEntityFromDatastore("user", sessionIDName, cookie.getValue(), datastore); //returns null if it doesn't exist
  }


}
