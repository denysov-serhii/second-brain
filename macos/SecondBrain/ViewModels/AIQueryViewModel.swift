import Foundation
import Observation
import os

enum AIQueryState {
    case idle
    case sending
    case success(URL)
    case error(String)
}

@Observable
final class AIQueryViewModel {
    private(set) var providers: [AIProviderConfiguration]
    private(set) var state: AIQueryState = .idle
    var selectedProviderIndex = 0

    private let networkService: NetworkService
    private let resultFileService: AIResultFileService
    private let logger = Logger(subsystem: "com.secondbrain.macos", category: "ai-query")

    init(
        networkService: NetworkService = NetworkService(),
        resultFileService: AIResultFileService = AIResultFileService(),
        providers: [AIProviderConfiguration] = AppConfiguration.aiProviders
    ) {
        self.networkService = networkService
        self.resultFileService = resultFileService
        self.providers = providers
    }

    var selectedProvider: AIProviderConfiguration? {
        guard providers.indices.contains(selectedProviderIndex) else { return nil }
        return providers[selectedProviderIndex]
    }

    func submit(prompt: String, attachmentURL: URL?) async {
        let trimmedPrompt = prompt.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmedPrompt.isEmpty || attachmentURL != nil else {
            state = .error("Please add text or attach a media file.")
            return
        }
        guard let provider = selectedProvider else {
            state = .error("No AI provider configured.")
            return
        }

        state = .sending

        do {
            let response = try await networkService.callAI(
                provider: provider,
                prompt: trimmedPrompt,
                fileUrl: attachmentURL
            )
            let fileURL = try resultFileService.saveResult(
                providerName: provider.name,
                prompt: trimmedPrompt,
                attachmentFileName: attachmentURL?.lastPathComponent,
                response: response
            )
            state = .success(fileURL)
        } catch {
            logger.error("AI request failed: \(error.localizedDescription)")
            state = .error("AI request failed. Check provider settings, API key, and network connection.")
        }
    }

    func resetState() {
        state = .idle
    }
}
