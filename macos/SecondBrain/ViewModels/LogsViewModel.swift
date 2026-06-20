import Foundation
import Observation
import SwiftData

@Observable
final class LogsViewModel {
    private(set) var logs: [MacLogEntry] = []
    private(set) var isLoading = false
    private(set) var errorMessage: String?

    private let networkService: NetworkService
    private var modelContext: ModelContext?

    init(networkService: NetworkService = NetworkService(
        baseURL: URL(string: "https://api.secondbrain.example")!
    )) {
        self.networkService = networkService
    }

    func configure(modelContext: ModelContext) {
        self.modelContext = modelContext
    }

    func loadLogs() {
        guard let modelContext else { return }
        isLoading = true
        errorMessage = nil

        do {
            let descriptor = FetchDescriptor<MacLogEntry>(
                sortBy: [SortDescriptor(\.createdAt, order: .reverse)]
            )
            logs = try modelContext.fetch(descriptor)
        } catch {
            errorMessage = "Failed to load logs: \(error.localizedDescription)"
        }

        isLoading = false
    }

    func syncFromServer() async {
        isLoading = true
        errorMessage = nil
        defer { isLoading = false }

        do {
            let remote = try await networkService.fetchLogs()
            guard let modelContext else { return }

            for dto in remote {
                let entry = MacLogEntry(
                    id: dto.id ?? UUID(),
                    rawContent: dto.rawContent,
                    extractedText: dto.extractedText,
                    summary: dto.summary,
                    logType: dto.logType,
                    createdAt: dto.createdAt ?? .now,
                    sourceDevice: dto.sourceDevice ?? "UNKNOWN"
                )
                modelContext.insert(entry)
            }
            try modelContext.save()
            loadLogs()
        } catch {
            errorMessage = "Sync failed: \(error.localizedDescription)"
        }
    }
}
