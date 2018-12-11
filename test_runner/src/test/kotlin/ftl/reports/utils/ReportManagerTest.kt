package ftl.reports.utils

import com.google.common.truth.Truth.assertThat
import ftl.args.AndroidArgs
import ftl.reports.util.ReportManager
import ftl.reports.xml.model.JUnitTestCase
import ftl.reports.xml.model.JUnitTestResult
import ftl.reports.xml.model.JUnitTestSuite
import ftl.run.TestRunner
import ftl.test.util.FlankTestRunner
import org.junit.Rule
import org.junit.Test
import org.junit.contrib.java.lang.system.SystemErrRule
import org.junit.contrib.java.lang.system.SystemOutRule
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

@RunWith(FlankTestRunner::class)
class ReportManagerTest {

    @Rule
    @JvmField
    val systemErrRule = SystemErrRule().muteForSuccessfulTests()!!

    @Rule
    @JvmField
    val systemOutRule = SystemOutRule().muteForSuccessfulTests()!!

    @Test
    fun generate_fromErrorResult() {
        val matrix = TestRunner.matrixPathToObj("./src/test/kotlin/ftl/fixtures/error_result")
        val mockArgs = mock(AndroidArgs::class.java)
        `when`(mockArgs.smartFlankGcsPath).thenReturn("")
        ReportManager.generate(matrix, mockArgs)
    }

    @Test
    fun generate_fromSuccessResult() {
        val matrix = TestRunner.matrixPathToObj("./src/test/kotlin/ftl/fixtures/success_result")
        val mockArgs = mock(AndroidArgs::class.java)
        `when`(mockArgs.smartFlankGcsPath).thenReturn("")
        ReportManager.generate(matrix, mockArgs)
    }

    @Test
    fun createShardEfficiencyListTest() {
        val oldRunTestCases = mutableListOf(
            JUnitTestCase("a", "a", "10.0"),
            JUnitTestCase("b", "b", "20.0"),
            JUnitTestCase("c", "c", "30.0")
        )
        val oldRunSuite = JUnitTestSuite("", "-1", "-1", "-1", "-1", "-1", "-1", "-1", oldRunTestCases, null, null, null)
        val oldTestResult = JUnitTestResult(mutableListOf(oldRunSuite))

        val newRunTestCases = mutableListOf(
            JUnitTestCase("a", "a", "9.0"),
            JUnitTestCase("b", "b", "21.0"),
            JUnitTestCase("c", "c", "30.0")
        )
        val newRunSuite = JUnitTestSuite("", "-1", "-1", "-1", "-1", "-1", "-1", "-1", newRunTestCases, null, null, null)
        val newTestResult = JUnitTestResult(mutableListOf(newRunSuite))

        val mockArgs = mock(AndroidArgs::class.java)

        `when`(mockArgs.testShardChunks).thenReturn(listOf(listOf("class a#a"), listOf("class b#b"), listOf("class c#c")))
        val result = ReportManager.createShardEfficiencyList(oldTestResult, newTestResult, mockArgs)

        // We can do a better assertion when we decide on the efficiency calculation
        assertThat(result[0].expectedTime).isEqualTo(10.0)
        assertThat(result[0].finalTime).isEqualTo(9.0)

        assertThat(result[1].expectedTime).isEqualTo(20.0)
        assertThat(result[1].finalTime).isEqualTo(21.0)

        assertThat(result[2].expectedTime).isEqualTo(30.0)
        assertThat(result[2].finalTime).isEqualTo(30.0)
    }
}
