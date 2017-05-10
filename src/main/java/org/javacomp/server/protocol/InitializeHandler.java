package org.javacomp.server.protocol;

import org.javacomp.server.Request;
import org.javacomp.server.Server;

/**
 * Handles "initialize" method.
 *
 * <p>See
 * https://github.com/Microsoft/language-server-protocol/blob/master/protocol.md#initialize-request
 */
public class InitializeHandler extends RequestHandler<InitializeParams> {
  private final Server server;

  public InitializeHandler(Server server) {
    super("initialize", InitializeParams.class);
    this.server = server;
  }

  @Override
  public InitializeResult handleRequest(Request<InitializeParams> request) {
    InitializeParams params = request.getParams();
    server.initialize(params.processId, params.rootUri, params.initializationOptions);
    InitializeResult result = new InitializeResult();
    result.capabilities = new InitializeResult.ServerCapabilities();
    result.capabilities.textDocumentSync = new InitializeResult.TextDocumentSyncOptions();
    result.capabilities.textDocumentSync.openClose = true;
    result.capabilities.textDocumentSync.change = InitializeResult.TextDocumentSyncKind.INCREMENTAL;
    result.capabilities.completionProvider = new InitializeResult.CompletionOptions();
    result.capabilities.completionProvider.triggerCharacters = new String[] {"."};
    return result;
  }
}
