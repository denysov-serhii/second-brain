import AppKit
import Foundation
import Observation

@Observable
final class ResultFilesViewModel {
    private(set) var files: [AIResultFile] = []

    private let resultFileService: AIResultFileService

    init(resultFileService: AIResultFileService = AIResultFileService()) {
        self.resultFileService = resultFileService
    }

    func loadFiles() {
        files = resultFileService.listResultFiles()
    }

    func open(_ file: AIResultFile) {
        NSWorkspace.shared.open(file.url)
    }

    func reveal(_ file: AIResultFile) {
        NSWorkspace.shared.activateFileViewerSelecting([file.url])
    }
}
