import Foundation

final class AIResultFileService {
    private let fileManager: FileManager
    private let resultsDirectory: URL

    init(fileManager: FileManager = .default) {
        self.fileManager = fileManager

        let appSupport = fileManager.urls(for: .applicationSupportDirectory, in: .userDomainMask).first
            ?? fileManager.temporaryDirectory
        self.resultsDirectory = appSupport
            .appendingPathComponent("SecondBrain", isDirectory: true)
            .appendingPathComponent("AIResults", isDirectory: true)
    }

    func saveResult(
        providerName: String,
        prompt: String,
        attachmentFileName: String?,
        response: String
    ) throws -> URL {
        try ensureDirectoryExists()

        let millis = Int(Date().timeIntervalSince1970 * 1000)
        let suffix = UUID().uuidString.prefix(8)
        let fileName = "ai_result_\(millis)_\(suffix).json"
        let targetURL = resultsDirectory.appendingPathComponent(fileName)

        let payload: [String: Any?] = [
            "timestamp": ISO8601DateFormatter().string(from: .now),
            "provider": providerName,
            "prompt": prompt,
            "attachment_file_name": attachmentFileName,
            "response": response
        ]
        let sanitized = payload.compactMapValues { $0 }
        let data = try JSONSerialization.data(withJSONObject: sanitized, options: [.prettyPrinted, .sortedKeys])
        try data.write(to: targetURL, options: .atomic)
        return targetURL
    }

    func listResultFiles() -> [AIResultFile] {
        try? ensureDirectoryExists()

        guard let urls = try? fileManager.contentsOfDirectory(
            at: resultsDirectory,
            includingPropertiesForKeys: [.creationDateKey, .contentModificationDateKey, .fileSizeKey, .isRegularFileKey],
            options: [.skipsHiddenFiles]
        ) else {
            return []
        }

        return urls.compactMap { url in
            guard let values = try? url.resourceValues(forKeys: [.creationDateKey, .contentModificationDateKey, .fileSizeKey, .isRegularFileKey]),
                  values.isRegularFile == true else {
                return nil
            }

            return AIResultFile(
                url: url,
                createdAt: values.creationDate ?? values.contentModificationDate ?? .distantPast,
                fileSizeBytes: Int64(values.fileSize ?? 0)
            )
        }
        .sorted { $0.createdAt > $1.createdAt }
    }

    private func ensureDirectoryExists() throws {
        if !fileManager.fileExists(atPath: resultsDirectory.path) {
            try fileManager.createDirectory(at: resultsDirectory, withIntermediateDirectories: true)
        }
    }
}
