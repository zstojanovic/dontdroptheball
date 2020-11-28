package org.dontdroptheball.shared.protocol;

import com.github.czyzby.websocket.serialization.SerializationException;
import com.github.czyzby.websocket.serialization.Transferable;
import com.github.czyzby.websocket.serialization.impl.Deserializer;
import com.github.czyzby.websocket.serialization.impl.Serializer;
import org.dontdroptheball.shared.Const;

import java.util.Arrays;

public class NewPlayerResponse implements Transferable<NewPlayerResponse> {
  static final NewPlayerResponse EXAMPLE = new NewPlayerResponse();
  public byte index;
  public ChatMessage[] messages;

  NewPlayerResponse() {
  }

  public NewPlayerResponse(byte index, ChatMessage[] messages) {
    this.index = index;
    this.messages = messages;
  }

  @Override
  public void serialize(Serializer serializer) throws SerializationException {
    serializer.serializeByte(index).serializeTransferableArray(messages);
  }

  @Override
  public NewPlayerResponse deserialize(Deserializer deserializer) throws SerializationException {
    var response = new NewPlayerResponse();
    response.index = deserializer.deserializeByte();
    var array = new ChatMessage[Const.MESSAGE_LIMIT];
    var length = deserializer.deserializeTransferableArray(array, ChatMessage.EXAMPLE);
    response.messages = Arrays.copyOf(array, length);
    return response;
  }
}
