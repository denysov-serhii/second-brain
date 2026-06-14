import Foundation
import SwiftData

@Model
final class MacLogEntry {
    var id: UUID
    var rawContent: String?
    var extractedText: String?
    var summary: String?
    var logType: String?
    var embedding: [Float]?
    var createdAt: Date
    var sourceDevice: String

    init(
        id: UUID = UUID(),
        rawContent: String? = nil,
        extractedText: String? = nil,
        summary: String? = nil,
        logType: String? = nil,
        embedding: [Float]? = nil,
        createdAt: Date = .now,
        sourceDevice: String = "MACOS"
    ) {
        self.id = id
        self.rawContent = rawContent
        self.extractedText = extractedText
        self.summary = summary
        self.logType = logType
        self.embedding = embedding
        self.createdAt = createdAt
        self.sourceDevice = sourceDevice
    }
}
