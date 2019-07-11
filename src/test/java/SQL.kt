import com.github.bryanser.syncmoney.SQLManager
import com.github.bryanser.syncmoney.SQLManager.pool
import com.github.bryanser.syncmoney.unaryPlus
import org.junit.Assert
import org.junit.Test

open class SQL{
    @Test
    fun test(){
        Class.forName("com.mysql.jdbc.Driver")
        SQLManager.init("jdbc:mysql://127.0.0.1:3306/test?user=root&password=123456")
        val conn = +pool
        val ps = conn.prepareStatement("SELECT money FROM syncmoney WHERE player = ?")
        ps.setString(1,"test")
        val rs = ps.executeQuery()
        Assert.assertTrue(rs.next())
        Assert.assertEquals(rs.getDouble("money"),2545.23, 1e-5)
    }
}