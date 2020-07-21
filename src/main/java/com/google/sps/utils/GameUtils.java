/ Copyright 2019 Google LLC
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
 
package com.google.sps.utils;
 
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.DatastoreService;
import java.util.ArrayList;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Key;
import com.google.sps.utils.UserUtils;
import java.util.logging.Logger;
 
public final class GameUtils {
 
  private static final Logger log = Logger.getLogger(GameUtils.class.getName());
 
  /**
  * Checks to see whether the user already has a game by the given name, and returns false if so
  */
  public static boolean IsValidGameName(String gameName, Entity userEntity) {
    
    if(userEntity == null){
        log.severe("found null user entity when checking for valid game name");
        return false;
    }
    if(gameName == ""){
        return false;
    }
    
    // list of game names for the given user
    ArrayList<String> names = (ArrayList<String>) userEntity.getProperty("gameNames");
 
    //compares game names to the prospective game name, and returns false if there's a match
    for(String name : names){
        if(name == gameName){
            return false;
        }
    }
    return true;
  }
 
  /**
  * Creates a game entity and returns the entity if successful
  */
  public static Entity createGameEntity(String gameName, DatastoreService datastore) {
    
    if(gameName == ""){
        return null;
    }
    if(datastore == null){
        log.severe("found null datastore trying to create game " + gameName);
        return null;
    }
    
    // intializing game property values
    Entity gameEntity = new Entity("Game");
    ArrayList<String> userIds = new ArrayList<String>();
    String quizQuestion = "";
    long quiz_timestamp = 0;
 
    gameEntity.setProperty("gameName", gameName);
    gameEntity.setProperty("userIds", userIds);
    gameEntity.setProperty("quizQuestion", quizQuestion);
    gameEntity.setProperty("quiz_timestamp", quiz_timestamp);
    Key gameKey = gameEntity.getKey();
 
    datastore.put(gameEntity);
 
    // getting the game id for the game id property
    Entity entity = null;
    try{
      entity = datastore.get(gameKey);
 
      String key = KeyFactory.keyToString(gameKey);
      entity.setProperty("gameId", key);
      datastore.put(entity);
    }catch(EntityNotFoundException e){
      log.severe("EntityNotFoundException; game entity not found when adding game id in create game");
      return null;
    }
 
    return entity;
  }
 
  /**
  * add user to user list in game entity, and creates a score entity for the given user and game
  */
  public static boolean addUserToGame(String userId, Entity gameEntity, DatastoreService datastore) {
 
    if(userId == ""){
      return false;
    }
    if(gameEntity == null){
        log.severe("found null game entity trying to user "+ userId +" to game");
        return false;
    }
    if(datastore == null){
        log.severe("found null datastore trying to game to user " + userId);
        return false;
    }
 
    ArrayList<String> userIds = (ArrayList<String>) gameEntity.getProperty("userIds");
    if(userIds == null){
      userIds = new ArrayList<String>();
    }
 
    // add user to game entity
    userIds.add(userId);
    gameEntity.setProperty("userIds", userIds);
    datastore.put(gameEntity);
    
    return true;
  }
 
  public static Entity startGame(String gameName, DatastoreService datastore) {
 
    if(datastore == null){
        log.severe("found null datastore trying to join game " + gameName);
        return null;
    }
 
    // create game entity
    Entity gameEntity = GameUtils.createGameEntity(gameName, datastore);
    if(gameEntity == null){
        log.severe("create game failed for game " + gameName);
        return null;
    }
 
    return gameEntity;
  }
 
  public static Entity joinGame(DatastoreService datastore, String gameId) {
 
    if(datastore == null){
        log.severe("found null datastore trying to join game " + gameId);
        return null;
    }
 
    // or join a game 
    Entity gameEntity = UserUtils.getEntityFromDatastore("Game", "gameId", gameId, datastore);
    if(gameEntity == null){ 
        return null;
    }
    
    return gameEntity;
  }
 
 
  public static boolean setGame(Entity userEntity, DatastoreService datastore, Entity gameEntity) {
 
    if(userEntity == null){
        log.severe("found null userEntity trying to join game " + gameName);
        return false;
    }
 
    if(datastore == null){
        log.severe("found null datastore trying to join game " + gameName + " with user " + (String) userEntity.getProperty("username"));
        return false;
    }

    if(gameEntity == null){
        log.severe("found null game Entity trying to join game " + gameName + " with user " + (String) userEntity.getProperty("username"));
        return false;
    }
 
    // add user to game entity + vice versa
    boolean userAdded = GameUtils.addUserToGame((String) userEntity.getProperty("userId"), gameEntity, datastore);
    if(!userAdded){
        log.severe("failed to add user to game " + gameName);
        return false;
    }
    boolean gameAdded = UserUtils.addGameToUser(userEntity, datastore, (String) gameEntity.getProperty("gameId"));
    if(!gameAdded){
        log.severe("failed to add game "  + gameName + " to user");
        return false;
    }
    
    return true;
  }
 
}
 
 
