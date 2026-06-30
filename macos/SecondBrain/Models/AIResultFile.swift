import Foundation

struct AIResultFile: Identifiable, Hashable {
    let url: URL
    let createdAt: Date
    let fileSizeBytes: Int64

    var id: URL { url }
    var fileName: String { url.lastPathComponent }
}
