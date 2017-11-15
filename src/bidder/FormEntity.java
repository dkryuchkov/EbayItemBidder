/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bidder;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;

@NotThreadSafe
public class FormEntity
  extends StringEntity
{
  public FormEntity(List<? extends NameValuePair> parameters, String charset)
    throws UnsupportedEncodingException
  {
    super(EncodedUtils.format(parameters, charset != null ? charset : HTTP.DEF_CONTENT_CHARSET.name()), ContentType.create("application/x-www-form-urlencoded", charset));
  }
  
  public FormEntity(Iterable<? extends NameValuePair> parameters, Charset charset)
  {
    super(EncodedUtils.format(parameters, charset != null ? charset : HTTP.DEF_CONTENT_CHARSET), ContentType.create("application/x-www-form-urlencoded", charset));
  }
  
  public FormEntity(List<? extends NameValuePair> parameters)
    throws UnsupportedEncodingException
  {
    this(parameters, (Charset)null);
  }
  
  public FormEntity(Iterable<? extends NameValuePair> parameters)
  {
    this(parameters, null);
  }
}