package infra

import java.sql.Timestamp
import java.time.LocalDate

class DateTimeProvider {
  def todaysMidnight(): Long = Timestamp.valueOf(LocalDate.now().atStartOfDay).getTime
}
