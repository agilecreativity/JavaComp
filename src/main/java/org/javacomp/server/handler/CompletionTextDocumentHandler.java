package org.javacomp.server.handler;

import com.google.gson.Gson;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.javacomp.completion.CompletionCandidate;
import org.javacomp.project.Project;
import org.javacomp.protocol.ClientCapabilities;
import org.javacomp.protocol.CompletionItem;
import org.javacomp.protocol.CompletionItem.CompletionItemKind;
import org.javacomp.protocol.CompletionItem.InsertTextFormat;
import org.javacomp.protocol.CompletionItem.ResolveAction;
import org.javacomp.protocol.CompletionItem.ResolveActionParams;
import org.javacomp.protocol.CompletionItem.ResolveData;
import org.javacomp.protocol.CompletionList;
import org.javacomp.protocol.TextDocumentPositionParams;
import org.javacomp.server.Request;
import org.javacomp.server.Server;

/**
 * Handles "textDocument/completion" notification.
 *
 * <p>See
 * https://github.com/Microsoft/language-server-protocol/blob/master/protocol.md#completion-request
 */
public class CompletionTextDocumentHandler extends RequestHandler<TextDocumentPositionParams> {
  private static final int MAX_CANDIDATES = 30;

  private final Server server;
  private final Gson gson;

  public CompletionTextDocumentHandler(Server server, Gson gson) {
    super("textDocument/completion", TextDocumentPositionParams.class);
    this.server = server;
    this.gson = gson;
  }

  @Override
  public CompletionList handleRequest(Request<TextDocumentPositionParams> request)
      throws Exception {
    TextDocumentPositionParams params = request.getParams();
    Project project = server.getProject();
    List<CompletionCandidate> candidates =
        project.getCompletionCandidates(
            Paths.get(params.textDocument.uri),
            params.position.getLine(),
            params.position.getCharacter());

    CompletionList completionList = new CompletionList();
    completionList.isIncomplete = (candidates.size() > MAX_CANDIDATES);
    completionList.items = new ArrayList<>();

    int len = Math.min(candidates.size(), MAX_CANDIDATES);
    for (int i = 0; i < len; i++) {
      CompletionCandidate candidate = candidates.get(i);
      CompletionItem item = new CompletionItem();
      item.label = candidate.getName();
      item.kind = getCompletionItemKind(candidate.getKind());
      Optional<String> detail = candidate.getDetail();
      if (detail.isPresent()) {
        item.detail = detail.get();
      }

      // The sort text of a candidate is the candidate label prefixed with its
      // sort category ordinal.
      char sortPrefix = (char) ('a' + candidate.getSortCategory().ordinal());
      item.sortText = sortPrefix + item.label;

      fillInsertText(item, candidate);
      fillData(item, candidate);

      completionList.items.add(item);
    }
    return completionList;
  }

  private void fillInsertText(CompletionItem item, CompletionCandidate candidate) {
    boolean supportsSnippet = clientSupportsSnippet(server.getClientCapabilities());
    item.insertTextFormat = supportsSnippet ? InsertTextFormat.SNIPPET : InsertTextFormat.PLAINTEXT;
    Optional<String> insertText = Optional.empty();
    if (supportsSnippet) {
      insertText = candidate.getInsertSnippet();
    }
    if (!insertText.isPresent()) {
      // Either the client doesn't snippet, or the candidate doesn't have a snippet.
      insertText = candidate.getInsertPlainText();
    }
    item.insertText = insertText.orElse(null);
  }

  private void fillData(CompletionItem item, CompletionCandidate candidate) {
    Map<ResolveAction, ResolveActionParams> resolveActions = candidate.getResolveActions();
    if (resolveActions.isEmpty()) {
      return;
    }

    item.data = new ArrayList<>(resolveActions.size());
    for (Map.Entry<ResolveAction, ResolveActionParams> entry : resolveActions.entrySet()) {
      ResolveAction action = entry.getKey();
      ResolveActionParams params = entry.getValue();
      ResolveData data = new ResolveData();
      data.action = action;
      data.params = gson.toJsonTree(params);
      item.data.add(data);
    }
  }

  private static boolean clientSupportsSnippet(ClientCapabilities clientCapabilities) {
    if (clientCapabilities.textDocument == null
        || clientCapabilities.textDocument.completion == null
        || clientCapabilities.textDocument.completion.completionItem == null) {
      return false;
    }
    return clientCapabilities.textDocument.completion.completionItem.snippetSupport;
  }

  private static CompletionItemKind getCompletionItemKind(CompletionCandidate.Kind candidateKind) {
    switch (candidateKind) {
      case CLASS:
        return CompletionItemKind.CLASS;
      case INTERFACE:
        return CompletionItemKind.INTERFACE;
      case ENUM:
        return CompletionItemKind.ENUM;
      case METHOD:
        return CompletionItemKind.METHOD;
      case VARIABLE:
        return CompletionItemKind.VARIABLE;
      case FIELD:
        return CompletionItemKind.FIELD;
      case PACKAGE:
        return CompletionItemKind.MODULE;
      case KEYWORD:
        return CompletionItemKind.KEYWORD;
      default:
        return CompletionItemKind.UNKNOWN;
    }
  }
}
