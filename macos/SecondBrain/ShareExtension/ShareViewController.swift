import AppKit
import UniformTypeIdentifiers

// MARK: - macOS Share Extension
//
// To add this share extension to the Xcode project:
// 1. In Xcode, select File → New → Target → Share Extension.
// 2. Name it "SecondBrainShareExtension".
// 3. Replace the generated principal class with this file.
// 4. Add an App Group (e.g. "group.com.secondbrain") to both targets
//    so the extension can communicate with the main app.
// 5. In the extension's Info.plist, set NSExtensionActivationRule to accept
//    text, URLs, images, and audio files as needed.

final class ShareViewController: NSViewController {

    private var loadingIndicator: NSProgressIndicator?

    override func loadView() {
        view = NSView(frame: NSRect(x: 0, y: 0, width: 400, height: 200))
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        setupUI()
        processSharedContent()
    }

    // MARK: - UI

    private func setupUI() {
        let label = NSTextField(labelWithString: "Saving to Second Brain…")
        label.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(label)

        let indicator = NSProgressIndicator()
        indicator.style = .spinning
        indicator.translatesAutoresizingMaskIntoConstraints = false
        indicator.startAnimation(nil)
        view.addSubview(indicator)
        loadingIndicator = indicator

        NSLayoutConstraint.activate([
            indicator.centerXAnchor.constraint(equalTo: view.centerXAnchor),
            indicator.centerYAnchor.constraint(equalTo: view.centerYAnchor, constant: -16),
            label.centerXAnchor.constraint(equalTo: view.centerXAnchor),
            label.topAnchor.constraint(equalTo: indicator.bottomAnchor, constant: 12)
        ])
    }

    // MARK: - Processing

    private func processSharedContent() {
        guard let extensionItem = extensionContext?.inputItems.first as? NSExtensionItem,
              let attachments = extensionItem.attachments else {
            done()
            return
        }

        Task {
            do {
                try await uploadItems(attachments)
            } catch {
                // Silently complete - the user will see the entry missing if sync fails
            }
            done()
        }
    }

    private func uploadItems(_ providers: [NSItemProvider]) async throws {
        let baseURL = URL(string: "https://api.secondbrain.example")!
        let networkService = NetworkService(baseURL: baseURL)

        for provider in providers {
            if provider.hasItemConformingToTypeIdentifier(UTType.plainText.identifier) {
                if let text = try? await loadText(from: provider) {
                    try await networkService.uploadLog(content: text, fileUrl: nil)
                }
            } else if provider.hasItemConformingToTypeIdentifier(UTType.url.identifier) {
                if let url = try? await loadURL(from: provider) {
                    try await networkService.uploadLog(content: url.absoluteString, fileUrl: nil)
                }
            } else if provider.hasItemConformingToTypeIdentifier(UTType.audio.identifier) {
                if let fileURL = try? await loadFile(from: provider, type: UTType.audio) {
                    try await networkService.uploadLog(content: nil, fileUrl: fileURL)
                }
            } else if provider.hasItemConformingToTypeIdentifier(UTType.image.identifier) {
                if let fileURL = try? await loadFile(from: provider, type: UTType.image) {
                    try await networkService.uploadLog(content: nil, fileUrl: fileURL)
                }
            }
        }
    }

    // MARK: - Item loading helpers

    private func loadText(from provider: NSItemProvider) async throws -> String? {
        return try await withCheckedThrowingContinuation { continuation in
            provider.loadItem(forTypeIdentifier: UTType.plainText.identifier, options: nil) { item, error in
                if let error {
                    continuation.resume(throwing: error)
                    return
                }
                if let text = item as? String {
                    continuation.resume(returning: text)
                } else {
                    continuation.resume(returning: nil)
                }
            }
        }
    }

    private func loadURL(from provider: NSItemProvider) async throws -> URL? {
        return try await withCheckedThrowingContinuation { continuation in
            provider.loadItem(forTypeIdentifier: UTType.url.identifier, options: nil) { item, error in
                if let error {
                    continuation.resume(throwing: error)
                    return
                }
                if let url = item as? URL {
                    continuation.resume(returning: url)
                } else {
                    continuation.resume(returning: nil)
                }
            }
        }
    }

    private func loadFile(from provider: NSItemProvider, type: UTType) async throws -> URL? {
        return try await withCheckedThrowingContinuation { continuation in
            provider.loadItem(forTypeIdentifier: type.identifier, options: nil) { item, error in
                if let error {
                    continuation.resume(throwing: error)
                    return
                }
                if let url = item as? URL {
                    continuation.resume(returning: url)
                } else {
                    continuation.resume(returning: nil)
                }
            }
        }
    }

    // MARK: - Completion

    private func done() {
        extensionContext?.completeRequest(returningItems: [], completionHandler: nil)
    }
}
