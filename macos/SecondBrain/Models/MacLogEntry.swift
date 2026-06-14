import Foundation
import SwiftData

@Model
final class MacLogEntry {
    var id: UUID
    var raw_content: String?
    var extracted_text: String?
    var summary: String?
    var log_type: String?
    var embedding: [Float]?
    var created_at: Date
    var source_device: String

    init(
        id: UUID = UUID(),
        raw_content: String? = nil,
        extracted_text: String? = nil,
        summary: String? = nil,
        log_type: String? = nil,
        embedding: [Float]? = nil,
        created_at: Date = .now,
        source_device: String = "MACOS"
    ) {
        self.id = id
        self.raw_content = raw_content
        self.extracted_text = extracted_text
        self.summary = summary
        self.log_type = log_type
        self.embedding = embedding
        self.created_at = created_at
        self.source_device = source_device
    }
}
