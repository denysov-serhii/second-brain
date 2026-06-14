import Foundation
import UniformTypeIdentifiers

enum NetworkError: Error {
    case badResponse
    case invalidStatusCode(Int)
}

final class NetworkService {
    private let ingestURL: URL
    private let session: URLSession

    init(baseURL: URL, session: URLSession = .shared) {
        self.ingestURL = baseURL.appending(path: "/api/v1/logs/ingest")
        self.session = session
    }

    func uploadLog(content: String?, fileUrl: URL?) async throws {
        let boundary = "Boundary-\(UUID().uuidString)"
        var request = URLRequest(url: ingestURL)
        request.httpMethod = "POST"
        request.setValue("multipart/form-data; boundary=\(boundary)", forHTTPHeaderField: "Content-Type")

        var body = Data()
        body.appendMultipartField(name: "raw_content", value: content ?? "", boundary: boundary)
        body.appendMultipartField(name: "source_device", value: "MACOS", boundary: boundary)

        if let fileUrl {
            let fileData = try Data(contentsOf: fileUrl)
            let filename = fileUrl.lastPathComponent
            let mimeType = mimeType(for: fileUrl)
            body.appendMultipartFile(name: "file", filename: filename, mimeType: mimeType, data: fileData, boundary: boundary)
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

    private func mimeType(for fileUrl: URL) -> String {
        if let type = UTType(filenameExtension: fileUrl.pathExtension)?.preferredMIMEType {
            return type
        }
        return "application/octet-stream"
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
