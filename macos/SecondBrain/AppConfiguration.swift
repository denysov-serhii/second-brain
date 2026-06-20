import Foundation

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
}
