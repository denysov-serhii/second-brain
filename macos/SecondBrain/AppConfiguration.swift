import Foundation

struct AIProviderConfiguration: Identifiable, Hashable {
    let name: String
    let endpoint: URL
    let apiKeyHeader: String
    let apiKeyPrefix: String
    let apiKey: String

    var id: String { name }
}

enum AppConfiguration {
    /// Base URL for the Second Brain API server.
    /// Override at build time by setting the `API_BASE_URL` environment variable
    /// or by providing a custom Info.plist entry named `APIBaseURL`.
    static let apiBaseURL: URL = {
        if let plistValue = Bundle.main.object(forInfoDictionaryKey: "APIBaseURL") as? String,
           let url = URL(string: plistValue) {
            return url
        }
        return URL(string: "https://api.secondbrain.example")!
    }()

    /// Configurable AI providers. Can be overridden via Info.plist key `AIProviders`.
    /// Format:
    /// [
    ///   {
    ///     "name": "OpenAI",
    ///     "endpoint": "https://api.openai.com/v1/responses",
    ///     "apiKeyHeader": "Authorization",
    ///     "apiKeyPrefix": "Bearer ",
    ///     "apiKey": "..."
    ///   }
    /// ]
    static let aiProviders: [AIProviderConfiguration] = {
        if let items = Bundle.main.object(forInfoDictionaryKey: "AIProviders") as? [[String: Any]] {
            let providers = items.compactMap { item -> AIProviderConfiguration? in
                guard let name = item["name"] as? String,
                      let endpointString = item["endpoint"] as? String,
                      let endpoint = URL(string: endpointString) else {
                    return nil
                }
                return AIProviderConfiguration(
                    name: name,
                    endpoint: endpoint,
                    apiKeyHeader: item["apiKeyHeader"] as? String ?? "Authorization",
                    apiKeyPrefix: item["apiKeyPrefix"] as? String ?? "Bearer ",
                    apiKey: item["apiKey"] as? String ?? ""
                )
            }
            if !providers.isEmpty {
                return providers
            }
        }

        return [
            AIProviderConfiguration(
                name: "OpenAI",
                endpoint: URL(string: "https://api.openai.com/v1/responses")!,
                apiKeyHeader: "Authorization",
                apiKeyPrefix: "Bearer ",
                apiKey: ""
            ),
            AIProviderConfiguration(
                name: "Gemini",
                endpoint: URL(string: "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent")!,
                apiKeyHeader: "x-goog-api-key",
                apiKeyPrefix: "",
                apiKey: ""
            )
        ]
    }()
}
