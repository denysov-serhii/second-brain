import Foundation
import UniformTypeIdentifiers

enum NetworkError: Error {
    case badResponse
    case invalidStatusCode(Int)
}

struct LogEntryDTO: Decodable {
    let id: UUID?
    let rawContent: String?
    let extractedText: String?
    let summary: String?
    let logType: String?
    let sourceDevice: String?
    let createdAt: Date?

    enum CodingKeys: String, CodingKey {
        case id
        case rawContent = "raw_content"
        case extractedText = "extracted_text"
        case summary
        case logType = "log_type"
        case sourceDevice = "source_device"
        case createdAt = "created_at"
    }
}

final class NetworkService {
    private let baseURL: URL
    private let ingestURL: URL
    private let session: URLSession

    init(baseURL: URL = AppConfiguration.apiBaseURL, session: URLSession = .shared) {
        self.baseURL = baseURL
        self.ingestURL = baseURL.appending(path: "/api/v1/logs/ingest")
        self.session = session
    }

    func fetchLogs(page: Int = 0, size: Int = 50) async throws -> [LogEntryDTO] {
        var components = URLComponents(
            url: baseURL.appending(path: "/api/v1/logs"),
            resolvingAgainstBaseURL: false
        )!
        components.queryItems = [
            URLQueryItem(name: "page", value: "\(page)"),
            URLQueryItem(name: "size", value: "\(size)")
        ]
        guard let url = components.url else { throw NetworkError.badResponse }

        let (data, response) = try await session.data(from: url)
        guard let httpResponse = response as? HTTPURLResponse else {
            throw NetworkError.badResponse
        }
        guard (200...299).contains(httpResponse.statusCode) else {
            throw NetworkError.invalidStatusCode(httpResponse.statusCode)
        }

        let decoder = JSONDecoder()
        decoder.dateDecodingStrategy = .iso8601
        return try decoder.decode([LogEntryDTO].self, from: data)
    }

    func uploadLog(content: String?, fileUrl: URL?) async throws {
        let boundary = multipartBoundary()
        var request = URLRequest(url: ingestURL)
        request.httpMethod = "POST"
        request.setValue(
            "multipart/form-data; boundary=\(boundary)",
            forHTTPHeaderField: "Content-Type"
        )

        var body = Data()
        body.appendMultipartField(name: "raw_content", value: content ?? "", boundary: boundary)
        body.appendMultipartField(name: "source_device", value: "MACOS", boundary: boundary)

        if let fileUrl {
            let fileData = try Data(contentsOf: fileUrl)
            let filename = fileUrl.lastPathComponent
            let mime = mimeType(for: fileUrl)
            body.appendMultipartFile(
                name: "file",
                filename: filename,
                mimeType: mime,
                data: fileData,
                boundary: boundary
            )
        }

        body.appendString("--\(boundary)--\r\n")
        request.httpBody = body

        let (_, response) = try await session.data(for: request)
        guard let httpResponse = response as? HTTPURLResponse else {
            throw NetworkError.badResponse
        }
        guard (200...299).contains(httpResponse.statusCode) else {
            throw NetworkError.invalidStatusCode(httpResponse.statusCode)
        }
    }

    func callAI(
        provider: AIProviderConfiguration,
        prompt: String,
        fileUrl: URL?
    ) async throws -> String {
        var request = URLRequest(url: provider.endpoint)
        request.httpMethod = "POST"

        let apiKey = provider.apiKey.trimmingCharacters(in: .whitespacesAndNewlines)
        if !apiKey.isEmpty {
            request.setValue(
                provider.apiKeyPrefix + apiKey,
                forHTTPHeaderField: provider.apiKeyHeader
            )
        }

        let boundary = multipartBoundary()
        request.setValue(
            "multipart/form-data; boundary=\(boundary)",
            forHTTPHeaderField: "Content-Type"
        )

        var body = Data()
        body.appendMultipartField(name: "prompt", value: prompt, boundary: boundary)

        if let fileUrl {
            let fileData = try Data(contentsOf: fileUrl)
            body.appendMultipartFile(
                name: "file",
                filename: fileUrl.lastPathComponent,
                mimeType: mimeType(for: fileUrl),
                data: fileData,
                boundary: boundary
            )
        }

        body.appendString("--\(boundary)--\r\n")
        request.httpBody = body

        let (data, response) = try await session.data(for: request)
        guard let httpResponse = response as? HTTPURLResponse else {
            throw NetworkError.badResponse
        }
        guard (200...299).contains(httpResponse.statusCode) else {
            throw NetworkError.invalidStatusCode(httpResponse.statusCode)
        }

        if let jsonObject = try? JSONSerialization.jsonObject(with: data),
           JSONSerialization.isValidJSONObject(jsonObject),
           let prettyData = try? JSONSerialization.data(withJSONObject: jsonObject, options: [.prettyPrinted]),
           let text = String(data: prettyData, encoding: .utf8) {
            return text
        }

        return String(decoding: data, as: UTF8.self)
    }

    private func mimeType(for fileUrl: URL) -> String {
        UTType(filenameExtension: fileUrl.pathExtension)?.preferredMIMEType ?? "application/octet-stream"
    }

    private func multipartBoundary() -> String {
        "Boundary\(UUID().uuidString.replacingOccurrences(of: "-", with: ""))"
    }
}

private extension Data {
    mutating func appendString(_ value: String) {
        if let data = value.data(using: .utf8) {
            append(data)
        }
    }

    mutating func appendMultipartField(name: String, value: String, boundary: String) {
        appendString("--\(boundary)\r\n")
        appendString("Content-Disposition: form-data; name=\"\(name)\"\r\n\r\n")
        appendString("\(value)\r\n")
    }

    mutating func appendMultipartFile(
        name: String,
        filename: String,
        mimeType: String,
        data: Data,
        boundary: String
    ) {
        appendString("--\(boundary)\r\n")
        appendString("Content-Disposition: form-data; name=\"\(name)\"; filename=\"\(filename)\"\r\n")
        appendString("Content-Type: \(mimeType)\r\n\r\n")
        append(data)
        appendString("\r\n")
    }
}
