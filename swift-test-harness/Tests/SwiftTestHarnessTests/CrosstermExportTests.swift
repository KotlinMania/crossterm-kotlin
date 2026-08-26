#if canImport(Testing)
import Testing
import Crossterm

@Suite("Crossterm Swift Export Tests")
struct CrosstermExportTests {
    @Test("Crossterm swift module imported cleanly")
    func testSwiftModuleLoads() throws {
        #expect(Bool(true), "Crossterm swift module imported cleanly")
    }
}
#elseif canImport(XCTest)
import XCTest
import Crossterm

final class CrosstermExportTests: XCTestCase {
    func testSwiftModuleLoads() throws {
        XCTAssertTrue(true, "Crossterm swift module imported cleanly")
    }
}
#endif

