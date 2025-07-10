import IosTestProject
import XCTest
import Shared

final class ThrowsTest: XCTestCase {
    func testThrows() async throws {
        do {
            try await Foo().coFoo()
            XCTFail("No error thrown")
        } catch {
            assert(error.kotlinException is KotlinRuntimeException)
        }
    }
}
