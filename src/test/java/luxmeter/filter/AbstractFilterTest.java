package luxmeter.filter;

import static luxmeter.Util.NO_RESONSE_RETURNED_YET;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.sun.net.httpserver.Filter;

@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractFilterTest {
    @Mock
    private Filter.Chain chain;

    protected Filter.Chain getMockedChain() {
        return chain;
    }

    protected void checkResponseCodeAndChaining(int actualStatusCode, int expectedStatusCode) throws IOException {
        assertThat(actualStatusCode, equalTo(expectedStatusCode));
        if (expectedStatusCode == NO_RESONSE_RETURNED_YET) {
            verify(getMockedChain(), times(1)).doFilter(any());
        }
        else {
            verify(getMockedChain(), never()).doFilter(any());
        }
    }
}
