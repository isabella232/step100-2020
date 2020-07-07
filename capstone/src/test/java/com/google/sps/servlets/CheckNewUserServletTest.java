package com.google.sps.servlets;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

import com.google.sps.Objects.User;
import com.google.sps.Objects.Badge;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.ArrayList;

/**
 * Unit tests for {@link CheckNewUserServlet}.
 */
 @RunWith(JUnit4.class)
public class CheckNewUserServletTest {
  private static final String CURRENT_USER_EMAIL = "test@mctest.com";
  private static final String CURRENT_USER_ID = "testy-mc-test";
  private static final String OTHER_USER_EMAIL = "test2@mctest.com";
  private static final String OTHER_USER_ID = "testytwo-mc-test";

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
            new LocalDatastoreServiceTestConfig()
                .setDefaultHighRepJobPolicyUnappliedJobPercentage(0),
            new LocalUserServiceTestConfig())
          .setEnvEmail(CURRENT_USER_EMAIL)
          .setEnvIsLoggedIn(true)
          .setEnvAuthDomain("gmail.com")
          .setEnvAttributes(
              new HashMap(
                  ImmutableMap.of(
                      "com.google.appengine.api.users.UserService.user_id_key", CURRENT_USER_ID)));

  private static final User CURRENT_USER = new User(CURRENT_USER_ID, "Test", "McTest", 
                          CURRENT_USER_EMAIL, 
                          /* phoneNumber= */ "123-456-7890", 
                          /* profilePic= */ "", 
                          /* badges= */ new LinkedHashSet<Badge>(), 
                          /* groups= */ new LinkedHashSet<Long>(), 
                          /* interests= */ new ArrayList<String>());

  private static final User OTHER_USER = new User(OTHER_USER_ID, "Test Two", "McTest", 
                          OTHER_USER_EMAIL, 
                          /* phoneNumber= */ "123-456-7890", 
                          /* profilePic= */ "", 
                          /* badges= */ new LinkedHashSet<Badge>(), 
                          /* groups= */ new LinkedHashSet<Long>(), 
                          /* interests= */ new ArrayList<String>());                        

  @Mock private HttpServletRequest mockRequest;
  @Mock private HttpServletResponse mockResponse;
  private StringWriter responseWriter;
  private DatastoreService datastore;
  private CheckNewUserServlet checkNewUserServlet;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    helper.setUp();
    datastore = DatastoreServiceFactory.getDatastoreService();

    addUserToDatastore(datastore, OTHER_USER);

    // Set up a fake HTTP response.
    responseWriter = new StringWriter();
    when(mockResponse.getWriter()).thenReturn(new PrintWriter(responseWriter));

    checkNewUserServlet = new CheckNewUserServlet();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void doPost_newUserVisiting() throws Exception {
    checkNewUserServlet.doGet(mockRequest, mockResponse);
    String response = responseWriter.toString();
    assertThat(response).contains(/* isUserNew= */ "true");
  }

  @Test
  public void doPost_existingUserVisiting() throws Exception {
    addUserToDatastore(datastore, CURRENT_USER);

    checkNewUserServlet.doGet(mockRequest, mockResponse);
    String response = responseWriter.toString();
    assertThat(response).contains(/* isUserNew= */ "false");

    removeUserFromDatastore(datastore, CURRENT_USER);
  }

  @Test
  public void doGet_userNotLoggedIn() throws Exception {
    helper.setEnvIsLoggedIn(false);

    checkNewUserServlet.doGet(mockRequest, mockResponse);
    String response = responseWriter.toString();
    assertThat(response).contains(/* isUserNew= */ "false");

    helper.setEnvIsLoggedIn(true);
  }

  private void addUserToDatastore(DatastoreService datastore, User user) {
    datastore.put(user.toEntity());
  }

  private void removeUserFromDatastore(DatastoreService datastore, User user) {
    Key entityKey = KeyFactory.createKey("User", user.getUserId());
    datastore.delete(entityKey);
  }
}