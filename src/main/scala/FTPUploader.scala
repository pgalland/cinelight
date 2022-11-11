import FTPUploader.{host, login, password}
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPReply

import java.io.BufferedOutputStream
import java.io.Closeable
import java.io.IOException
import java.nio.charset.StandardCharsets

object FTPUploader {
  val host: String = sys.env("CINE_HOST")
  val login: String = sys.env("CINE_LOGIN")
  val password: String = sys.env("CINE_PASSWORD")
}

trait Uploader extends Closeable {
  def uploadFile(filename: String, content: String): Unit
}

class FTPUploader extends Uploader {
  private val ftpClient = new FTPClient()
  ftpClient.connect(host)
  ftpClient.login(login, password)
  println(s"Connected to $host.")
  print(ftpClient.getReplyString)
  if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode)) {
    ftpClient.disconnect()
    throw new IOException("FTP server refused connection.")
  }

  override def uploadFile(filename: String, content: String): Unit = {
    ftpClient.changeWorkingDirectory("/www")
    System.out.print(ftpClient.getReplyString)
    ftpClient.enterLocalPassiveMode()
    ftpClient.setFileType(FTP.BINARY_FILE_TYPE)
    val os = new BufferedOutputStream(ftpClient.storeFileStream(filename))
    try {
      os.write(content.getBytes(StandardCharsets.UTF_8))
    } finally {
      if (os != null) os.close()
    }
    ftpClient.completePendingCommand
    println(ftpClient.getReplyString)
  }

  override def close(): Unit = {
    ftpClient.logout
    if (ftpClient.isConnected) ftpClient.disconnect()
  }
}

class LocalUploader extends Uploader {
  private val www = os.pwd / "www"

  override def uploadFile(filename: String, content: String): Unit = {
    os.makeDir.all(www)
    os.write(www / filename, content)
  }

  override def close(): Unit = {}
}
