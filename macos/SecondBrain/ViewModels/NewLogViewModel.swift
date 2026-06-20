import Foundation
import Observation
import SwiftData

enum NewLogState {
    case idle
    case saving
    case success
    case error(String)
}

@Observable
final class NewLogViewModel {
    private(set) var saveState: NewLogState = .idle

    private let networkService: NetworkService
    private var modelContext: ModelContext?

    init(networkService: NetworkService = NetworkService()) {
        self.networkService = networkService
    }

    func configure(modelContext: ModelContext) {
        self.modelContext = modelContext
    }

    func saveTextLog(content: String) async {
        guard !content.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else { return }
        saveState = .saving
        do {
            try await networkService.uploadLog(content: content, fileUrl: nil)
            persistLocally(rawContent: content, logType: "PERSONAL", fileUrl: nil)
            saveState = .success
        } catch {
            saveState = .error("Failed to save: \(error.localizedDescription)")
        }
    }

    func saveAudioLog(fileUrl: URL) async {
        saveState = .saving
        do {
            try await networkService.uploadLog(content: nil, fileUrl: fileUrl)
            persistLocally(rawContent: nil, logType: "AUDIO", fileUrl: fileUrl)
            saveState = .success
        } catch {
            saveState = .error("Failed to save: \(error.localizedDescription)")
        }
    }

    func resetState() {
        saveState = .idle
    }

    private func persistLocally(rawContent: String?, logType: String, fileUrl: URL?) {
        guard let modelContext else { return }
        let entry = MacLogEntry(
            rawContent: rawContent ?? fileUrl?.lastPathComponent,
            logType: logType,
            sourceDevice: "MACOS"
        )
        modelContext.insert(entry)
        try? modelContext.save()
    }
}
