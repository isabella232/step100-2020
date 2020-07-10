package com.google.sps.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.sps.Objects.Option;
import com.google.sps.Objects.comparator.OptionsComparator;
import com.google.sps.Objects.response.OptionsAndUserVotedOptions;
import com.google.sps.Objects.response.PollResponse;

@WebServlet("/poll")
public class PollServlet extends AuthenticatedServlet {

  @Override
  public void doGet(String userId, HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    Query query = new Query("Option").addSort("timestamp", SortDirection.ASCENDING);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);
    OptionsAndUserVotedOptions optionsAndVotedOptions =
        this.getOptionsAndVotedOptions(results, userId);
    List<Option> options = optionsAndVotedOptions.getOptions();
    List<Long> votedOptions = optionsAndVotedOptions.getVotedOptions();
    // Sort list of options based on number of votes
    Collections.sort(options, new OptionsComparator());
    PollResponse pollResponse = new PollResponse(options, votedOptions, userId);
    ServletHelper.write(response, pollResponse, "application/json");
  }

  @Override
  public void doPost(String userId, HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    String text = request.getParameter("text");
    Option option = new Option(0, text, new ArrayList<String>());
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(option.toEntity());
  }

  /**
   * Gets all options in the poll and options for which current user has voted for and returns both
   * in the form of a OptionsAndUserVotedOptions object.
   *
   * @param results query results
   * @param userId user id
   * @return OptionsAndUserVotedOptions object
   */
  private OptionsAndUserVotedOptions getOptionsAndVotedOptions(
      PreparedQuery results, String userId) {
    List<Option> options = new ArrayList<Option>();
    /*
     * List to keep track of options current user has voted for so that checkboxes
     * can be marked as checked on frontend side
     */
    List<Long> votedOptions = new ArrayList<Long>();
    for (Entity entity : results.asIterable()) {
      Option option = Option.fromEntity(entity);
      List<String> votes = option.getVotes();
      long id = option.getId();
      options.add(option);
      // If current user voted for option, add to list of voted options
      if (votes != null && votes.contains(userId)) {
        votedOptions.add(id);
      }
    }
    return new OptionsAndUserVotedOptions(options, votedOptions);
  }
}
